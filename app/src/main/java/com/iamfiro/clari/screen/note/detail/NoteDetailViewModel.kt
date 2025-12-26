package com.iamfiro.clari.screen.note.detail

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.NoteRepository
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.TranscriptLine
import kotlinx.coroutines.Job
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
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: Job? = null

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

    /**
     * 현재 재생 위치에 해당하는 transcript 인덱스 및 word 인덱스 업데이트
     */
    private fun updateCurrentTranscriptIndex() {
        val transcripts = _uiState.value.note?.transcripts ?: return
        val words = _uiState.value.note?.words
        val currentPos = _uiState.value.currentPositionMs
        
        val transcriptIndex = transcripts.indexOfLast { transcript ->
            transcript.startMs <= currentPos
        }
        
        // 단어 인덱스 업데이트
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
    
    /**
     * 특정 transcript로 이동
     */
    fun seekToTranscript(transcript: TranscriptLine) {
        seekTo(transcript.startMs)
        if (!_uiState.value.isPlaying && _uiState.value.isMediaReady) {
            togglePlayPause()
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

    /**
     * 음성 재생 정지 및 리소스 해제
     */
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
