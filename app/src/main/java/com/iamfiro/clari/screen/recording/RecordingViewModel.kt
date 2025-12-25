package com.iamfiro.clari.screen.recording

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.Repository.NoteRepository
import com.iamfiro.clari.core.service.AudioRecorderService
import com.iamfiro.clari.core.service.ConnectionState
import com.iamfiro.clari.core.service.RecordingState
import com.iamfiro.clari.core.service.WebSocketService
import com.iamfiro.clari.core.service.model.SttResponse
import com.iamfiro.clari.core.service.model.TranscriptItem
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.NoteType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class RecordingViewModel(
    context: Context,
    private val noteRepository: NoteRepository,
    private val languageCode: String = "ko-KR"
) : ViewModel() {
    
    companion object {
        private const val TAG = "RecordingViewModel"
    }
    
    private val webSocketService = WebSocketService()
    private val audioRecorderService = AudioRecorderService(context)

    val connectionState: StateFlow<ConnectionState> = webSocketService.connectionState

    val recordingState = audioRecorderService.recordingState

    // 현재 실시간 인식 텍스트 (partial)
    val partialText: StateFlow<SttResponse?> = webSocketService.partialText

    // 확정된 텍스트 목록 (committed + formatted)
    private val _transcriptItems = MutableStateFlow<List<TranscriptItem>>(emptyList())
    val transcriptItems: StateFlow<List<TranscriptItem>> = _transcriptItems.asStateFlow()

    // 녹음 진행 중 여부
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // 경과 시간 (초)
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()
    
    private var timerJob: Job? = null

    private var audioChunksSent = 0

    private var itemIdCounter = 0
    
    init {
        Log.d(TAG, "========== RecordingViewModel 초기화 ==========")

        viewModelScope.launch {
            Log.d(TAG, "오디오 청크 수집 시작")
            audioRecorderService.audioChunks.collect { base64Audio ->
                audioChunksSent++
                if (audioChunksSent % 50 == 1) {
                    Log.d(TAG, "오디오 청크 전송 #$audioChunksSent (길이: ${base64Audio.length})")
                }
                webSocketService.sendAudio(base64Audio)
            }
        }

        viewModelScope.launch {
            Log.d(TAG, "Committed 텍스트 수집 시작")
            webSocketService.committedTexts.collect { response ->
                Log.d(TAG, "========== Committed 텍스트 수신 ==========")
                Log.d(TAG, "Text: ${response.text}")
                Log.d(TAG, "Chunks: ${response.chunks}")

                val newItem = TranscriptItem(
                    id = itemIdCounter++,
                    committedText = response.text,
                    committedChunks = response.chunks
                )
                
                _transcriptItems.value = _transcriptItems.value + newItem
                Log.d(TAG, "총 아이템 수: ${_transcriptItems.value.size}")
            }
        }

        viewModelScope.launch {
            Log.d(TAG, "Formatted 텍스트 수집 시작")
            webSocketService.formattedTexts.collect { response ->
                Log.d(TAG, "========== Formatted 텍스트 수신 ==========")
                Log.d(TAG, "Text: ${response.text}")
                Log.d(TAG, "Chunks: ${response.chunks}")

                // 마지막 아이템을 찾아서 formatted로 업데이트
                val currentItems = _transcriptItems.value.toMutableList()
                if (currentItems.isNotEmpty()) {
                    // 아직 formatted가 적용되지 않은 가장 오래된 committed 찾기
                    val indexToUpdate = currentItems.indexOfFirst { !it.isFormatted }
                    
                    if (indexToUpdate != -1) {
                        val itemToUpdate = currentItems[indexToUpdate]
                        val updatedItem = itemToUpdate.copy(
                            formattedText = response.text,
                            formattedChunks = response.chunks,
                            isFormatted = true
                        )
                        currentItems[indexToUpdate] = updatedItem
                        _transcriptItems.value = currentItems
                        
                        Log.d(TAG, "아이템 #${itemToUpdate.id} formatted 적용 완료")
                        Log.d(TAG, "변경 전: ${itemToUpdate.committedChunks}")
                        Log.d(TAG, "변경 후: ${updatedItem.displayChunks}")
                    } else {
                        Log.w(TAG, "formatted 적용할 아이템 없음 (모두 이미 formatted)")
                    }
                } else {
                    Log.w(TAG, "formatted 적용할 아이템 없음 (목록 비어있음)")
                }
            }
        }

        viewModelScope.launch {
            webSocketService.connectionState.collect { state ->
                Log.d(TAG, "연결 상태 변경: $state")
            }
        }

        viewModelScope.launch {
            audioRecorderService.recordingState.collect { state ->
                Log.d(TAG, "녹음 상태 변경: $state")
                when (state) {
                    is RecordingState.Recording -> _isRecording.value = true
                    is RecordingState.Stopped -> _isRecording.value = false
                    else -> {}
                }
            }
        }
    }

    fun hasRecordPermission(): Boolean {
        return audioRecorderService.hasRecordPermission()
    }

    fun startRecording() {
        Log.d(TAG, "========== 녹음 시작 요청 ==========")
        audioChunksSent = 0
        
        // WebSocket 연결
        Log.d(TAG, "WebSocket 연결 시도...")
        webSocketService.connect()
        
        // 연결 완료 후 녹음 시작
        viewModelScope.launch {
            webSocketService.connectionState.collect { state ->
                Log.d(TAG, "연결 상태 확인: $state, 녹음 중: ${_isRecording.value}")
                if (state == ConnectionState.Connected && !_isRecording.value) {
                    Log.d(TAG, "WebSocket 연결 완료 - 녹음 시작")
                    audioRecorderService.startRecording()
                    startTimer()
                }
            }
        }
    }

    fun stopRecording() {
        Log.d(TAG, "========== 녹음 중지 요청 ==========")
        Log.d(TAG, "총 전송된 오디오 청크: $audioChunksSent")
        
        audioRecorderService.stopRecording()
        webSocketService.disconnect()
        stopTimer()
    }

    suspend fun saveNote(projectId: String): Note? {
        if (_elapsedSeconds.value == 0L) {
            Log.d(TAG, "녹음 시간이 0초이므로 저장하지 않음")
            return null
        }

        val note = Note(
            id = "${UUID.randomUUID()}",
            type = NoteType.NOT_READY,
            name = "녹음 ${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))}",
            duration = _elapsedSeconds.value * 1000,
            createdAt = LocalDateTime.now()
        )

        return try {
            val savedNote = noteRepository.createNote(note)
            Log.d(TAG, "노트 저장 완료: ${savedNote.id}")
            savedNote
        } catch (e: Exception) {
            Log.e(TAG, "노트 저장 실패", e)
            null
        }
    }

    fun toggleRecording() {
        Log.d(TAG, "녹음 토글 - 현재 상태: ${_isRecording.value}")
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _elapsedSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun formatElapsedTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }
    
    override fun onCleared() {
        Log.d(TAG, "========== ViewModel onCleared ==========")
        super.onCleared()
        audioRecorderService.release()
        webSocketService.release()
    }
}

class RecordingViewModelFactory(
    private val context: Context,
    private val noteRepository: NoteRepository,
    private val languageCode: String = "ko-KR"
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordingViewModel(context, noteRepository, languageCode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
