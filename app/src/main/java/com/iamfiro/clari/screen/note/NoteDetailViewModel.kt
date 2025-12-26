package com.iamfiro.clari.screen.note

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
    private val noteRepository: NoteRepository,
    private val noteId: String
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 오디오 재생 상태
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _isMediaReady = MutableStateFlow(false)
    val isMediaReady: StateFlow<Boolean> = _isMediaReady.asStateFlow()
    
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    // 현재 재생 중인 transcript 인덱스
    private val _currentTranscriptIndex = MutableStateFlow(-1)
    val currentTranscriptIndex: StateFlow<Int> = _currentTranscriptIndex.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: Job? = null

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            noteRepository.getNoteById(noteId)
                .onSuccess { note ->
                    _note.value = note
                    _totalDurationMs.value = note.duration
                    Log.d(TAG, "노트 로드 완료: ${note.name}, recordingUrl: ${note.recordingUrl}")
                    
                    // recordingUrl이 있으면 MediaPlayer 초기화
                    note.recordingUrl?.let { url ->
                        initMediaPlayer(url)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 로드 실패", e)
                    _error.value = "노트를 불러오는데 실패했습니다: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    private fun initMediaPlayer(url: String) {
        viewModelScope.launch {
            try {
                _isBuffering.value = true
                
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
                        _totalDurationMs.value = mp.duration.toLong()
                        _isMediaReady.value = true
                        _isBuffering.value = false
                        Log.d(TAG, "MediaPlayer 준비 완료, duration: ${mp.duration}ms")
                    }
                    
                    setOnCompletionListener {
                        _isPlaying.value = false
                        _currentPositionMs.value = _totalDurationMs.value
                        stopPositionUpdates()
                        Log.d(TAG, "재생 완료")
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer 오류: what=$what, extra=$extra")
                        _error.value = "오디오 재생 중 오류가 발생했습니다."
                        _isBuffering.value = false
                        true
                    }
                    
                    setOnBufferingUpdateListener { _, percent ->
                        Log.d(TAG, "버퍼링: $percent%")
                    }
                    
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "MediaPlayer 초기화 실패", e)
                _error.value = "오디오를 불러오는데 실패했습니다: ${e.message}"
                _isBuffering.value = false
            }
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition.toLong()
                        updateCurrentTranscriptIndex()
                    }
                }
                delay(100) // 100ms 마다 업데이트
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    /**
     * 현재 재생 위치에 해당하는 transcript 인덱스 업데이트
     */
    private fun updateCurrentTranscriptIndex() {
        val transcripts = _note.value?.transcripts ?: return
        val currentPos = _currentPositionMs.value
        
        val index = transcripts.indexOfLast { transcript ->
            transcript.startMs <= currentPos
        }
        
        if (index != _currentTranscriptIndex.value) {
            _currentTranscriptIndex.value = index
        }
    }

    fun refresh() {
        loadNote()
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        
        if (!_isMediaReady.value) {
            Log.w(TAG, "MediaPlayer가 아직 준비되지 않았습니다")
            return
        }
        
        if (_isPlaying.value) {
            mp.pause()
            _isPlaying.value = false
            stopPositionUpdates()
            Log.d(TAG, "일시정지")
        } else {
            mp.start()
            _isPlaying.value = true
            startPositionUpdates()
            Log.d(TAG, "재생 시작")
        }
    }

    fun skipForward(seconds: Int = 5) {
        val mp = mediaPlayer ?: return
        if (!_isMediaReady.value) return
        
        val newPosition = (mp.currentPosition + seconds * 1000).coerceAtMost(mp.duration)
        mp.seekTo(newPosition)
        _currentPositionMs.value = newPosition.toLong()
        updateCurrentTranscriptIndex()
    }

    fun skipBackward(seconds: Int = 5) {
        val mp = mediaPlayer ?: return
        if (!_isMediaReady.value) return
        
        val newPosition = (mp.currentPosition - seconds * 1000).coerceAtLeast(0)
        mp.seekTo(newPosition)
        _currentPositionMs.value = newPosition.toLong()
        updateCurrentTranscriptIndex()
    }

    fun seekTo(positionMs: Long) {
        val mp = mediaPlayer ?: return
        if (!_isMediaReady.value) return
        
        val clampedPosition = positionMs.coerceIn(0, mp.duration.toLong())
        mp.seekTo(clampedPosition.toInt())
        _currentPositionMs.value = clampedPosition
        updateCurrentTranscriptIndex()
    }
    
    /**
     * 특정 transcript로 이동
     */
    fun seekToTranscript(transcript: TranscriptLine) {
        seekTo(transcript.startMs)
        // 재생 중이 아니면 재생 시작
        if (!_isPlaying.value && _isMediaReady.value) {
            togglePlayPause()
        }
    }

    fun updateNoteName(newName: String) {
        viewModelScope.launch {
            noteRepository.updateNoteTitle(noteId, newName)
                .onSuccess { note ->
                    _note.value = note
                    Log.d(TAG, "노트 이름 변경 완료: ${note.name}")
                }
                .onFailure { e ->
                    Log.e(TAG, "노트 이름 변경 실패", e)
                    _error.value = "노트 이름 변경에 실패했습니다: ${e.message}"
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
                    _error.value = "노트 삭제에 실패했습니다: ${e.message}"
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        Log.d(TAG, "ViewModel 정리 완료")
    }
}
