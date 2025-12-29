package com.iamfiro.clari.screen.note.detail

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.NoteRepository
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.core.service.KeywordHit
import com.iamfiro.clari.feature.note.component.DetectedTerm
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.feature.note.model.TranscriptWord
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "NoteDetailViewModel"

class NoteDetailViewModel(
    private val noteId: String
) : ViewModel() {
    private val noteRepository = NoteRepository.getInstance()
    private val projectRepository = ProjectRepository.getInstance()
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: Job? = null

    private val triggeredHapticIds = mutableSetOf<String>()
    private var lastAutoDetectedWordIndex = -1

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            noteRepository.getNoteById(noteId)
                .onSuccess { note ->
                    _uiState.value = _uiState.value.copy(
                        note = note,
                        totalDurationMs = note.duration
                    )
                    Log.d(TAG, "노트 로드 완료: ${note.name}, recordingUrl: ${note.recordingUrl}")

                    note.recordingUrl?.let { url ->
                        initMediaPlayer(url)
                    }

                    if (note.keywordPackIds.isNotEmpty()) {
                        loadAvailableKeywords(note.keywordPackIds)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 로드 실패", e)
                    _uiState.value = _uiState.value.copy(error = "노트를 불러오는데 실패했습니다: ${e.message}")
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun initMediaPlayer(url: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isBuffering = true)
                
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    
                    setOnPreparedListener { mp ->
                        _uiState.value = _uiState.value.copy(
                            totalDurationMs = mp.duration.toLong(),
                            isMediaReady = true,
                            isBuffering = false
                        )
                        Log.d(TAG, "MediaPlayer 준비 완료, duration: ${mp.duration}ms")
                    }
                    
                    setOnCompletionListener {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = false,
                            currentPositionMs = _uiState.value.totalDurationMs
                        )
                        stopPositionUpdates()
                        Log.d(TAG, "재생 완료")
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer 오류: what=$what, extra=$extra")
                        _uiState.value = _uiState.value.copy(
                            error = "오디오 재생 중 오류가 발생했습니다.",
                            isBuffering = false
                        )
                        true
                    }
                    
                    setOnBufferingUpdateListener { _, percent ->
                        Log.d(TAG, "버퍼링: $percent%")
                    }
                    
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "MediaPlayer 초기화 실패", e)
                _uiState.value = _uiState.value.copy(
                    error = "오디오를 불러오는데 실패했습니다: ${e.message}",
                    isBuffering = false
                )
            }
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive && _uiState.value.isPlaying) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _uiState.value = _uiState.value.copy(currentPositionMs = mp.currentPosition.toLong())
                        updateCurrentTranscriptIndex()
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun updateCurrentTranscriptIndex() {
        val transcripts = _uiState.value.note?.transcripts ?: return
        val words = _uiState.value.note?.words
        val currentPos = _uiState.value.currentPositionMs
        
        val transcriptIndex = transcripts.indexOfLast { transcript ->
            transcript.startMs <= currentPos
        }

        val wordIndex = words?.indexOfLast { word ->
            word.startMs <= currentPos && currentPos <= word.endMs
        } ?: -1
        
        if (transcriptIndex != _uiState.value.currentTranscriptIndex || 
            wordIndex != _uiState.value.currentWordIndex) {
            _uiState.value = _uiState.value.copy(
                currentTranscriptIndex = transcriptIndex,
                currentWordIndex = wordIndex
            )
        }

        if (wordIndex >= 0 && wordIndex != lastAutoDetectedWordIndex && _uiState.value.isPlaying) {
            lastAutoDetectedWordIndex = wordIndex
            words?.getOrNull(wordIndex)?.let { currentWord ->
                checkAndAddKeyword(currentWord.text)
            }
        }
    }

    private fun checkAndAddKeyword(wordText: String) {
        val normalizedWord = wordText.lowercase().trim()
        val wordWithoutSpaces = normalizedWord.replace("\\s+".toRegex(), "")

        _uiState.value.availableKeywords.forEach { (keywordKey, term) ->
            val keywordWithoutSpaces = keywordKey.replace("\\s+".toRegex(), "")

            val exactMatch = normalizedWord.contains(keywordKey)

            val spaceIgnoredMatch = wordWithoutSpaces.contains(keywordWithoutSpaces)

            val words = keywordKey.split("\\s+".toRegex())
            val partialMatch = if (words.size > 1) {
                words.all { word -> normalizedWord.contains(word) }
            } else {
                false
            }
            
            if (exactMatch || spaceIgnoredMatch || partialMatch) {
                addTermToDisplay(term)
                Log.d(TAG, "키워드 매칭: '${term.keyword.name}' (텍스트: '$wordText', 정확:$exactMatch, 띄어쓰기무시:$spaceIgnoredMatch, 부분:$partialMatch)")
            }
        }
    }

    private fun addTermToDisplay(term: DetectedTerm) {
        val isNewTerm = !triggeredHapticIds.contains(term.id)
        
        val currentTerms = _uiState.value.displayedTerms
        val filteredTerms = currentTerms.filter { it.id != term.id }

        if (currentTerms.firstOrNull()?.id == term.id) return
        
        val updatedTerm = term.copy(detectedAt = System.currentTimeMillis())
        val newTerms = listOf(updatedTerm) + filteredTerms
        
        _uiState.value = _uiState.value.copy(
            displayedTerms = newTerms,
            shouldTriggerHaptic = isNewTerm
        )
        
        if (isNewTerm) {
            triggeredHapticIds.add(term.id)
        }
        
        Log.d(TAG, "키워드 추가: ${term.keyword.name}, 새 키워드: $isNewTerm, 총 ${newTerms.size}개")
    }

    fun onWordClicked(word: TranscriptWord) {
        checkAndAddKeyword(word.text)
        
        // 해당 위치로 이동
        seekTo(word.startMs)
        if (!_uiState.value.isPlaying && _uiState.value.isMediaReady) {
            togglePlayPause()
        }
    }

    fun onTranscriptClicked(transcript: TranscriptLine) {
        val words = _uiState.value.note?.words?.filter { word ->
            word.startMs >= transcript.startMs && word.startMs < transcript.endMs
        } ?: emptyList()

        words.forEach { word ->
            checkAndAddKeyword(word.text)
        }

        seekToTranscript(transcript)
    }

    fun onHapticTriggered() {
        _uiState.value = _uiState.value.copy(shouldTriggerHaptic = false)
    }

    fun refresh() {
        loadNote()
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        
        if (!_uiState.value.isMediaReady) {
            Log.w(TAG, "MediaPlayer가 아직 준비되지 않았습니다")
            return
        }
        
        if (_uiState.value.isPlaying) {
            mp.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
            stopPositionUpdates()
            Log.d(TAG, "일시정지")
        } else {
            mp.start()
            _uiState.value = _uiState.value.copy(isPlaying = true)
            startPositionUpdates()
            Log.d(TAG, "재생 시작")
        }
    }

    fun skipForward(seconds: Int = 5) {
        val mp = mediaPlayer ?: return
        if (!_uiState.value.isMediaReady) return
        
        val newPosition = (mp.currentPosition + seconds * 1000).coerceAtMost(mp.duration)
        mp.seekTo(newPosition)
        _uiState.value = _uiState.value.copy(currentPositionMs = newPosition.toLong())
        updateCurrentTranscriptIndex()
    }

    fun skipBackward(seconds: Int = 5) {
        val mp = mediaPlayer ?: return
        if (!_uiState.value.isMediaReady) return
        
        val newPosition = (mp.currentPosition - seconds * 1000).coerceAtLeast(0)
        mp.seekTo(newPosition)
        _uiState.value = _uiState.value.copy(currentPositionMs = newPosition.toLong())
        updateCurrentTranscriptIndex()
    }

    fun seekTo(positionMs: Long) {
        val mp = mediaPlayer ?: return
        if (!_uiState.value.isMediaReady) return
        
        val clampedPosition = positionMs.coerceIn(0, mp.duration.toLong())
        mp.seekTo(clampedPosition.toInt())
        _uiState.value = _uiState.value.copy(currentPositionMs = clampedPosition)
        updateCurrentTranscriptIndex()
    }

    fun seekToTranscript(transcript: TranscriptLine) {
        seekTo(transcript.startMs)
        if (!_uiState.value.isPlaying && _uiState.value.isMediaReady) {
            togglePlayPause()
        }
    }

    private fun loadAvailableKeywords(keywordPackIds: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingKeywords = true)
            
            try {
                val packs = keywordPackIds.map { packId ->
                    async {
                        projectRepository.getKeywordPackById(packId).getOrNull()
                    }
                }.awaitAll().filterNotNull()

                val linkedProjects = packs.map { pack ->
                    LinkedProject(id = pack.id, name = pack.name)
                }

                val keywordMap = mutableMapOf<String, DetectedTerm>()
                packs.forEach { pack ->
                    pack.word.forEach { word ->
                        val normalizedName = word.name.lowercase().trim()
                        if (!keywordMap.containsKey(normalizedName)) {
                            keywordMap[normalizedName] = DetectedTerm(
                                id = normalizedName,
                                keyword = KeywordHit(
                                    name = word.name,
                                    description = word.meaning
                                ),
                                detectedAt = 0L
                            )
                        }
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    linkedProjects = linkedProjects,
                    availableKeywords = keywordMap,
                    displayedTerms = emptyList(),
                    isLoadingKeywords = false
                )
                Log.d(TAG, "연결된 프로젝트 ${linkedProjects.size}개, 키워드 ${keywordMap.size}개 로드 완료")
            } catch (e: Exception) {
                Log.e(TAG, "키워드 로드 실패", e)
                _uiState.value = _uiState.value.copy(isLoadingKeywords = false)
            }
        }
    }

    fun updateNoteName(newName: String) {
        viewModelScope.launch {
            noteRepository.updateNoteTitle(noteId, newName)
                .onSuccess { note ->
                    _uiState.value = _uiState.value.copy(note = note)
                    Log.d(TAG, "노트 이름 변경 완료: ${note.name}")
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 이름 변경 실패", e)
                    _uiState.value = _uiState.value.copy(error = "노트 이름 변경에 실패했습니다: ${e.message}")
                }
        }
    }

    fun deleteNote(onSuccess: () -> Unit) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
                .onSuccess {
                    Log.d(TAG, "노트 삭제 완료: $noteId")
                    onSuccess()
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 삭제 실패", e)
                    _uiState.value = _uiState.value.copy(error = "노트 삭제에 실패했습니다: ${e.message}")
                }
        }
    }

    fun cleanup() {
        Log.d(TAG, "========== 음성 정지 및 리소스 해제 ==========")
        val mp = mediaPlayer
        if (mp != null) {
            try {
                if (_uiState.value.isPlaying) {
                    mp.pause()
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                    Log.d(TAG, "음성 재생 정지")
                }
                stopPositionUpdates()
                mp.release()
                mediaPlayer = null
                _uiState.value = _uiState.value.copy(
                    isMediaReady = false,
                    isBuffering = false
                )
                Log.d(TAG, "MediaPlayer 해제 완료")
            } catch (e: Exception) {
                Log.e(TAG, "MediaPlayer 해제 중 오류", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
        Log.d(TAG, "ViewModel 정리 완료")
    }
}
