package com.iamfiro.clari.screen.recording

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.repository.RecordingRepository
import com.iamfiro.clari.core.service.AudioRecorderService
import com.iamfiro.clari.core.service.KeywordHit
import com.iamfiro.clari.core.service.RecordingSessionService
import com.iamfiro.clari.core.service.RecordingState
import com.iamfiro.clari.core.service.ResourceHint
import com.iamfiro.clari.core.service.SessionConnectionState
import com.iamfiro.clari.feature.note.component.DetectedTerm
import com.iamfiro.clari.feature.note.component.WordDeckState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecordingViewModel(
    context: Context,
    private val recordingRepository: RecordingRepository,
    private val languageCode: String = "ko",
    private val keywordPackIds: List<String> = emptyList(),
    private val externalResourceIds: List<String> = emptyList()
) : ViewModel() {
    
    companion object {
        private const val TAG = "RecordingViewModel"
    }
    
    private val recordingSessionService = RecordingSessionService(ApiClient.getTokenManager())
    private val audioRecorderService = AudioRecorderService(context)

    val connectionState: StateFlow<SessionConnectionState> = recordingSessionService.connectionState

    val recordingState = audioRecorderService.recordingState

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val _noteId = MutableStateFlow<String?>(null)
    val noteId: StateFlow<String?> = _noteId.asStateFlow()

    val partialText: StateFlow<String?> = recordingSessionService.partialText

    private val _transcriptItems = MutableStateFlow<List<TranscriptItem>>(emptyList())
    val transcriptItems: StateFlow<List<TranscriptItem>> = _transcriptItems.asStateFlow()

    private val _detectedKeywords = MutableStateFlow<List<KeywordHit>>(emptyList())
    val detectedKeywords: StateFlow<List<KeywordHit>> = _detectedKeywords.asStateFlow()

    private val wordDeckState = WordDeckState()
    val detectedTerms: StateFlow<List<DetectedTerm>> = wordDeckState.terms
    val shouldTriggerHaptic: StateFlow<Boolean> = wordDeckState.shouldTriggerHaptic

    private val _resourceHints = MutableStateFlow<List<ResourceHint>>(emptyList())
    val resourceHints: StateFlow<List<ResourceHint>> = _resourceHints.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isCreatingSession = MutableStateFlow(false)
    val isCreatingSession: StateFlow<Boolean> = _isCreatingSession.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var timerJob: Job? = null
    private var itemIdCounter = 0
    private var keywordDismissJob: Job? = null

    init {
        Log.d(TAG, "========== RecordingViewModel 초기화 ==========")
        Log.d(TAG, "Language: $languageCode")
        Log.d(TAG, "KeywordPacks: $keywordPackIds")
        Log.d(TAG, "ExternalResources: $externalResourceIds")

        viewModelScope.launch {
            audioRecorderService.audioChunks.collect { base64Audio ->
                recordingSessionService.sendAudio(base64Audio)
            }
        }

        viewModelScope.launch {
            recordingSessionService.committedTexts.collect { text ->
                Log.d(TAG, "Committed: $text")
                val newItem = TranscriptItem(
                    id = itemIdCounter++,
                    text = text,
                    isFormatted = false
                )
                _transcriptItems.value = _transcriptItems.value + newItem
            }
        }

        viewModelScope.launch {
            recordingSessionService.formattedTexts.collect { text ->
                Log.d(TAG, "Formatted: $text")
                val currentItems = _transcriptItems.value.toMutableList()
                val indexToUpdate = currentItems.indexOfFirst { !it.isFormatted }
                if (indexToUpdate != -1) {
                    currentItems[indexToUpdate] = currentItems[indexToUpdate].copy(
                        text = text,
                        isFormatted = true
                    )
                    _transcriptItems.value = currentItems
                }
            }
        }

        viewModelScope.launch {
            recordingSessionService.detectedKeywords.collect { newKeywords ->
                val currentKeywords = _detectedKeywords.value
                
                // 키워드 리스트가 변경된 경우
                if (newKeywords != currentKeywords) {
                    _detectedKeywords.value = newKeywords
                    
                    if (newKeywords.isEmpty()) {
                        keywordDismissJob?.cancel()
                    } else if (newKeywords.size > currentKeywords.size || currentKeywords.isEmpty()) {
                        startKeywordDismissTimer()
                    }
                }

                newKeywords.forEach { keyword ->
                    wordDeckState.onTermDetected(keyword)
                }
            }
        }

        viewModelScope.launch {
            recordingSessionService.resourceHints.collect { hints ->
                _resourceHints.value = hints
                // 10초 후 자동 제거
                delay(10000)
                if (_resourceHints.value == hints) {
                    _resourceHints.value = emptyList()
                }
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
        
        if (_isCreatingSession.value) {
            Log.w(TAG, "이미 세션 생성 중")
            return
        }
        
        _isCreatingSession.value = true
        _error.value = null
        
        viewModelScope.launch {
            recordingRepository.createSession(
                title = null,
                languageCode = languageCode,
                keywordPackIds = keywordPackIds,
                externalResourceIds = externalResourceIds
            ).onSuccess { response ->
                Log.d(TAG, "세션 생성 성공: ${response.sessionId}")
                _sessionId.value = response.sessionId
                _noteId.value = response.noteId

                recordingSessionService.connect(response.sessionId)

                try {
                    val readyState = connectionState.first { state ->
                        state is SessionConnectionState.Ready ||
                        state is SessionConnectionState.Connected ||
                        state is SessionConnectionState.Error
                    }
                    
                    when (readyState) {
                        is SessionConnectionState.Ready, is SessionConnectionState.Connected -> {
                            Log.d(TAG, "연결 준비 완료 ($readyState) - 녹음 시작")
                            audioRecorderService.startRecording()
                            startTimer()
                            _isCreatingSession.value = false
                        }
                        is SessionConnectionState.Error -> {
                            Log.e(TAG, "연결 실패: ${readyState.message}")
                            _error.value = readyState.message
                            _isCreatingSession.value = false
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "연결 대기 중 오류", e)
                    _error.value = "연결 실패: ${e.message}"
                    _isCreatingSession.value = false
                }
            }.onFailure { e ->
                Log.e(TAG, "세션 생성 실패", e)
                _error.value = "세션 생성 실패: ${e.message}"
                _isCreatingSession.value = false
            }
        }
    }

    fun stopRecording(onComplete: ((String?) -> Unit)? = null) {
        Log.d(TAG, "========== 녹음 중지 요청 ==========")
        
        audioRecorderService.stopRecording()
        stopTimer()
        
        val currentSessionId = _sessionId.value
        val savedNoteId = _noteId.value
        
        if (currentSessionId != null) {
            viewModelScope.launch {
                recordingRepository.stopSession(currentSessionId)
                    .onSuccess { response ->
                        Log.d(TAG, "세션 중지 성공: ${response.message}")
                        recordingSessionService.disconnect()
                        // 상태 초기화
                        resetRecordingState()
                        onComplete?.invoke(savedNoteId)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "세션 중지 실패", e)
                        _error.value = "녹음 저장 실패: ${e.message}"
                        recordingSessionService.disconnect()
                        // 상태 초기화
                        resetRecordingState()
                        onComplete?.invoke(null)
                    }
            }
        } else {
            recordingSessionService.disconnect()
            // 상태 초기화
            resetRecordingState()
            onComplete?.invoke(null)
        }
    }

    fun cancelRecording() {
        Log.d(TAG, "========== 녹음 취소 요청 ==========")
        
        audioRecorderService.stopRecording()
        stopTimer()
        
        val currentSessionId = _sessionId.value
        if (currentSessionId != null) {
            viewModelScope.launch {
                recordingRepository.cancelSession(currentSessionId)
                    .onSuccess {
                        Log.d(TAG, "세션 취소 성공")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "세션 취소 실패", e)
                    }
                
                recordingSessionService.disconnect()
                resetRecordingState()
            }
        } else {
            recordingSessionService.disconnect()
            resetRecordingState()
        }
    }

    fun toggleRecording() {
        Log.d(TAG, "녹음 토글 - 현재 상태: ${_isRecording.value}")
        if (_isRecording.value) {
            audioRecorderService.stopRecording()
            stopTimer()
        } else {
            audioRecorderService.startRecording()
            startTimer()
        }
    }

    fun setKeywordEnabled(enabled: Boolean) {
        recordingSessionService.setKeywordEnabled(enabled)
    }

    fun setHintsEnabled(enabled: Boolean) {
        recordingSessionService.setHintsEnabled(enabled)
    }

    fun onHapticTriggered() {
        wordDeckState.onHapticTriggered()
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

    private fun startKeywordDismissTimer() {
        keywordDismissJob?.cancel()
        keywordDismissJob = viewModelScope.launch {
            while (_detectedKeywords.value.isNotEmpty()) {
                delay(5000)

                val currentKeywords = _detectedKeywords.value
                if (currentKeywords.isNotEmpty()) {
                    val updatedKeywords = currentKeywords.dropLast(1)
                    _detectedKeywords.value = updatedKeywords

                    if (updatedKeywords.isNotEmpty()) {
                        // 다음 카드의 타이머는 이미 시작됨 (while 루프가 계속됨)
                    }
                }
            }
        }
    }

    fun resetRecordingState() {
        Log.d(TAG, "========== 녹음 상태 초기화 ==========")
        
        audioRecorderService.stopRecording()
        stopTimer()
        recordingSessionService.disconnect()
        
        _sessionId.value = null
        _noteId.value = null
        _transcriptItems.value = emptyList()
        _detectedKeywords.value = emptyList()
        _resourceHints.value = emptyList()
        _isRecording.value = false
        _isCreatingSession.value = false
        _elapsedSeconds.value = 0
        _error.value = null
        itemIdCounter = 0

        wordDeckState.clear()
        
        Log.d(TAG, "✅ 상태 초기화 완료 - 다시 녹음 가능")
    }
    
    override fun onCleared() {
        Log.d(TAG, "========== ViewModel onCleared ==========")
        super.onCleared()
        audioRecorderService.release()
        recordingSessionService.release()
    }
}

data class TranscriptItem(
    val id: Int,
    val text: String,
    val isFormatted: Boolean = false
)

class RecordingViewModelFactory(
    private val context: Context,
    private val recordingRepository: RecordingRepository,
    private val languageCode: String = "ko",
    private val keywordPackIds: List<String> = emptyList(),
    private val externalResourceIds: List<String> = emptyList()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordingViewModel(
                context, 
                recordingRepository, 
                languageCode,
                keywordPackIds,
                externalResourceIds
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
