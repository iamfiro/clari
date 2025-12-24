package com.iamfiro.clari.core.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 마이크 녹음 서비스
 * PCM 16bit, 16kHz, Mono 형식으로 오디오 캡처
 * Base64로 인코딩하여 전송
 */
class AudioRecorderService(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioRecorderService"
        
        // 오디오 설정 (가이드 문서에 따름)
        private const val SAMPLE_RATE = 16000           // 16kHz
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        
        // 버퍼 크기 (약 100ms 분량)
        private const val BUFFER_SIZE_FACTOR = 2
    }
    
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false
    
    // 디버깅용 카운터
    private var chunkCount = 0
    
    // Base64로 인코딩된 오디오 청크를 방출
    private val _audioChunks = MutableSharedFlow<String>(replay = 0)
    val audioChunks: SharedFlow<String> = _audioChunks.asSharedFlow()
    
    // 녹음 상태
    private val _recordingState = MutableSharedFlow<RecordingState>(replay = 1)
    val recordingState: SharedFlow<RecordingState> = _recordingState.asSharedFlow()
    
    /**
     * 마이크 권한 확인
     */
    fun hasRecordPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "마이크 권한 확인: $hasPermission")
        return hasPermission
    }
    
    /**
     * 최소 버퍼 크기 계산
     */
    private fun getMinBufferSize(): Int {
        val minSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        val bufferSize = minSize * BUFFER_SIZE_FACTOR
        
        Log.d(TAG, "최소 버퍼 크기: $minSize, 사용할 버퍼 크기: $bufferSize")
        return bufferSize
    }
    
    /**
     * 녹음 시작
     */
    fun startRecording() {
        Log.d(TAG, "========== 녹음 시작 요청 ==========")
        
        if (isRecording) {
            Log.w(TAG, "이미 녹음 중. 중복 시작 방지")
            return
        }
        
        if (!hasRecordPermission()) {
            Log.e(TAG, "마이크 권한 없음!")
            CoroutineScope(Dispatchers.Main).launch {
                _recordingState.emit(RecordingState.Error("마이크 권한이 필요합니다"))
            }
            return
        }
        
        val bufferSize = getMinBufferSize()
        chunkCount = 0
        
        try {
            Log.d(TAG, "AudioRecord 생성 중...")
            Log.d(TAG, "- Sample Rate: $SAMPLE_RATE")
            Log.d(TAG, "- Channel: MONO")
            Log.d(TAG, "- Format: PCM 16BIT")
            Log.d(TAG, "- Buffer Size: $bufferSize")
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            Log.d(TAG, "AudioRecord 상태: ${audioRecord?.state}")
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord 초기화 실패!")
                CoroutineScope(Dispatchers.Main).launch {
                    _recordingState.emit(RecordingState.Error("오디오 레코더 초기화 실패"))
                }
                return
            }
            
            Log.d(TAG, "AudioRecord 녹음 시작...")
            audioRecord?.startRecording()
            isRecording = true
            
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "녹음 루프 시작")
                _recordingState.emit(RecordingState.Recording)
                
                val buffer = ShortArray(bufferSize / 2)  // 16bit = 2 bytes
                
                while (isActive && isRecording) {
                    val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    
                    if (readResult > 0) {
                        chunkCount++
                        
                        // Short 배열을 Byte 배열로 변환 (Little Endian)
                        val byteBuffer = ByteArray(readResult * 2)
                        for (i in 0 until readResult) {
                            byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8 and 0xFF).toByte()
                        }
                        
                        // Base64 인코딩
                        val base64Audio = Base64.encodeToString(byteBuffer, Base64.NO_WRAP)
                        
                        // 매 50번째 청크마다 상세 로그
                        if (chunkCount % 50 == 1) {
                            Log.d(TAG, "========== 오디오 청크 #$chunkCount ==========")
                            Log.d(TAG, "읽은 샘플 수: $readResult")
                            Log.d(TAG, "바이트 크기: ${byteBuffer.size}")
                            Log.d(TAG, "Base64 길이: ${base64Audio.length}")
                            
                            // 오디오 레벨 확인 (디버깅용)
                            val maxAmplitude = buffer.take(readResult).maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 0
                            Log.d(TAG, "최대 진폭: $maxAmplitude")
                        }
                        
                        _audioChunks.emit(base64Audio)
                    } else {
                        Log.w(TAG, "AudioRecord read 실패: $readResult")
                    }
                }
                
                Log.d(TAG, "녹음 루프 종료. 총 청크 수: $chunkCount")
            }
            
            Log.d(TAG, "녹음 시작 완료")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}", e)
            CoroutineScope(Dispatchers.Main).launch {
                _recordingState.emit(RecordingState.Error("마이크 권한이 거부되었습니다"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "녹음 시작 실패: ${e.message}", e)
            CoroutineScope(Dispatchers.Main).launch {
                _recordingState.emit(RecordingState.Error("녹음 시작 실패: ${e.message}"))
            }
        }
    }
    
    /**
     * 녹음 중지
     */
    fun stopRecording() {
        Log.d(TAG, "========== 녹음 중지 ==========")
        Log.d(TAG, "총 청크 수: $chunkCount")
        
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        
        try {
            audioRecord?.stop()
            Log.d(TAG, "AudioRecord 중지됨")
            audioRecord?.release()
            Log.d(TAG, "AudioRecord 해제됨")
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord 중지/해제 실패: ${e.message}", e)
        }
        audioRecord = null
        
        CoroutineScope(Dispatchers.Main).launch {
            _recordingState.emit(RecordingState.Stopped)
        }
        
        Log.d(TAG, "녹음 중지 완료")
    }
    
    /**
     * 리소스 해제
     */
    fun release() {
        Log.d(TAG, "========== 리소스 해제 ==========")
        stopRecording()
    }
}

/**
 * 녹음 상태
 */
sealed class RecordingState {
    data object Idle : RecordingState()
    data object Recording : RecordingState()
    data object Stopped : RecordingState()
    data class Error(val message: String) : RecordingState()
}
