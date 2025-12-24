package com.iamfiro.clari.core.service

import android.util.Log
import com.iamfiro.clari.core.config.ApiConfig
import com.iamfiro.clari.core.service.model.AudioMessage
import com.iamfiro.clari.core.service.model.SttResponse
import com.iamfiro.clari.core.service.model.SttResponseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketService {
    
    companion object {
        private const val TAG = "WebSocketService"
    }
    
    private var webSocket: WebSocket? = null
    private val json = Json { ignoreUnknownKeys = true }
    
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _sttResponses = MutableSharedFlow<SttResponse>(replay = 0)
    val sttResponses: SharedFlow<SttResponse> = _sttResponses.asSharedFlow()

    private val _partialText = MutableStateFlow<SttResponse?>(null)
    val partialText: StateFlow<SttResponse?> = _partialText.asStateFlow()

    private val _committedTexts = MutableSharedFlow<SttResponse>(replay = 0)
    val committedTexts: SharedFlow<SttResponse> = _committedTexts.asSharedFlow()

    private val _formattedTexts = MutableSharedFlow<SttResponse>(replay = 0)
    val formattedTexts: SharedFlow<SttResponse> = _formattedTexts.asSharedFlow()

    private var sendCount = 0

    fun connect() {
        Log.d(TAG, "========== WebSocket 연결 시도 ==========")
        Log.d(TAG, "현재 상태: ${_connectionState.value}")
        
        if (_connectionState.value == ConnectionState.Connected ||
            _connectionState.value == ConnectionState.Connecting) {
            Log.w(TAG, "이미 연결 중이거나 연결됨. 중복 연결 방지")
            return
        }
        
        _connectionState.value = ConnectionState.Connecting
        
        val endpoint = ApiConfig.WebSocket.STT_ENDPOINT
        Log.d(TAG, "연결 URL: $endpoint")
        
        val request = Request.Builder()
            .url(endpoint)
            .build()
        
        Log.d(TAG, "Request 생성 완료: $request")
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "========== WebSocket 연결 성공 ==========")
                Log.d(TAG, "Response: $response")
                Log.d(TAG, "Response Code: ${response.code}")
                Log.d(TAG, "Response Message: ${response.message}")
                Log.d(TAG, "Response Headers: ${response.headers}")
                
                CoroutineScope(Dispatchers.Main).launch {
                    _connectionState.value = ConnectionState.Connected
                }
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "========== 메시지 수신 (텍스트) ==========")
                Log.d(TAG, "수신 데이터: $text")
                
                CoroutineScope(Dispatchers.Main).launch {
                    handleMessage(text)
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "========== 메시지 수신 (바이너리) ==========")
                Log.d(TAG, "수신 데이터 크기: ${bytes.size} bytes")
                Log.d(TAG, "수신 데이터 (UTF-8): ${bytes.utf8()}")
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "========== WebSocket 닫히는 중 ==========")
                Log.d(TAG, "Code: $code, Reason: $reason")
                
                webSocket.close(1000, null)
                CoroutineScope(Dispatchers.Main).launch {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "========== WebSocket 연결 종료 ==========")
                Log.d(TAG, "Code: $code, Reason: $reason")
                
                CoroutineScope(Dispatchers.Main).launch {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "========== WebSocket 연결 실패 ==========")
                Log.e(TAG, "에러 메시지: ${t.message}")
                Log.e(TAG, "에러 타입: ${t::class.java.simpleName}")
                Log.e(TAG, "Response: $response")
                Log.e(TAG, "Stack Trace:", t)
                
                CoroutineScope(Dispatchers.Main).launch {
                    _connectionState.value = ConnectionState.Error(t.message ?: "연결 실패")
                }
            }
        })
        
        Log.d(TAG, "WebSocket 객체 생성 완료: $webSocket")
    }

    private suspend fun handleMessage(text: String) {
        Log.d(TAG, "========== 메시지 파싱 시작 ==========")
        
        try {
            val response = json.decodeFromString<SttResponse>(text)
            Log.d(TAG, "파싱 성공 - Type: ${response.type}")
            Log.d(TAG, "파싱 성공 - Text: ${response.text}")
            Log.d(TAG, "파싱 성공 - Chunks: ${response.chunks}")
            
            _sttResponses.emit(response)
            
            when (SttResponseType.fromString(response.type)) {
                SttResponseType.PARTIAL -> {
                    Log.d(TAG, "PARTIAL 응답 처리")
                    _partialText.value = response
                }
                SttResponseType.COMMITTED -> {
                    Log.d(TAG, "COMMITTED 응답 처리")
                    _partialText.value = null
                    _committedTexts.emit(response)
                }
                SttResponseType.FORMATTED -> {
                    Log.d(TAG, "FORMATTED 응답 처리")
                    _formattedTexts.emit(response)
                }
                null -> {
                    Log.w(TAG, "알 수 없는 응답 타입: ${response.type}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "메시지 파싱 실패: ${e.message}")
            Log.e(TAG, "원본 데이터: $text")
            Log.e(TAG, "Stack Trace:", e)
        }
    }

    fun sendAudio(base64Audio: String) {
        sendCount++
        
        if (_connectionState.value != ConnectionState.Connected) {
            Log.w(TAG, "[전송 #$sendCount] 연결되지 않음. 현재 상태: ${_connectionState.value}")
            return
        }
        
        try {
            val message = AudioMessage(audio = base64Audio)
            val jsonMessage = json.encodeToString(message)
            
            // 매 10번째 전송마다 상세 로그
            if (sendCount % 10 == 1) {
                Log.d(TAG, "========== 오디오 전송 #$sendCount ==========")
                Log.d(TAG, "Base64 길이: ${base64Audio.length}")
                Log.d(TAG, "JSON 메시지 길이: ${jsonMessage.length}")
                Log.d(TAG, "JSON 메시지 앞부분: ${jsonMessage.take(100)}...")
            }
            
            val success = webSocket?.send(jsonMessage) ?: false
            
            if (sendCount % 10 == 1) {
                Log.d(TAG, "전송 결과: $success")
            }
            
            if (!success) {
                Log.e(TAG, "[전송 #$sendCount] 전송 실패!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[전송 #$sendCount] 전송 중 에러: ${e.message}")
            Log.e(TAG, "Stack Trace:", e)
        }
    }

    fun disconnect() {
        Log.d(TAG, "========== WebSocket 연결 해제 ==========")
        Log.d(TAG, "총 전송 횟수: $sendCount")
        
        webSocket?.close(1000, "사용자 종료")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
        _partialText.value = null
        sendCount = 0
        
        Log.d(TAG, "연결 해제 완료")
    }

    fun release() {
        Log.d(TAG, "========== WebSocket 리소스 해제 ==========")
        disconnect()
        client.dispatcher.executorService.shutdown()
        Log.d(TAG, "리소스 해제 완료")
    }
}

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
