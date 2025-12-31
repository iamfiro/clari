package com.iamfiro.clari.core.service

import android.util.Log
import com.iamfiro.clari.core.config.ApiConfig
import com.iamfiro.clari.core.network.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RecordingSessionService(private val tokenManager: TokenManager) {

    companion object {
        private const val TAG = "RecordingSessionService"
        private const val LOG_CHUNK = 3000
        private const val SEND_LOG_EVERY = 50
        private const val PRE_READY_QUEUE_LIMIT = 12
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val parseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var webSocket: WebSocket? = null

    @Volatile
    private var manualClose = false

    private val sendCount = AtomicInteger(0)

    // READY 전에 들어온 오디오를 소량만 버퍼링
    private val preReadyAudioQueue = ArrayDeque<String>(PRE_READY_QUEUE_LIMIT)

    private val _connectionState = MutableStateFlow<SessionConnectionState>(SessionConnectionState.Disconnected)
    val connectionState: StateFlow<SessionConnectionState> get() = _connectionState

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> get() = _sessionId

    private val _partialText = MutableStateFlow<String?>(null)
    val partialText: StateFlow<String?> get() = _partialText

    private val _committedTexts = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 64)
    val committedTexts: SharedFlow<String> get() = _committedTexts

    private val _formattedTexts = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 64)
    val formattedTexts: SharedFlow<String> get() = _formattedTexts

    private val _detectedKeywords = MutableSharedFlow<List<KeywordHit>>(replay = 1, extraBufferCapacity = 16)
    val detectedKeywords: SharedFlow<List<KeywordHit>> get() = _detectedKeywords

    private val _resourceHints = MutableSharedFlow<List<ResourceHint>>(replay = 1, extraBufferCapacity = 16)
    val resourceHints: SharedFlow<List<ResourceHint>> get() = _resourceHints

    private val _keywordEnabled = MutableStateFlow(true)
    val keywordEnabled: StateFlow<Boolean> get() = _keywordEnabled

    private val _hintsEnabled = MutableStateFlow(true)
    val hintsEnabled: StateFlow<Boolean> get() = _hintsEnabled

    fun connect(sessionId: String) {
        Log.d(TAG, "========== 녹음 세션 WebSocket 연결 시도 ==========")
        Log.d(TAG, "SessionId: $sessionId, State: ${_connectionState.value}")

        val st = _connectionState.value
        if (st is SessionConnectionState.Connecting ||
            st is SessionConnectionState.Connected ||
            st is SessionConnectionState.Ready
        ) {
            Log.w(TAG, "⚠️ 이미 연결 중/연결됨. 중복 연결 방지")
            return
        }

        manualClose = false
        preReadyAudioQueue.clear()
        _partialText.value = null
        _sessionId.value = sessionId
        _connectionState.value = SessionConnectionState.Connecting

        ioScope.launch {
            val token = tokenManager.getAccessTokenBlocking()
            if (token.isNullOrEmpty()) {
                mainScope.launch {
                    _connectionState.value = SessionConnectionState.Error("인증 토큰이 없습니다")
                }
                return@launch
            }

            val endpoint = ApiConfig.WebSocket.recordingSessionEndpoint(sessionId, token)

            val request = Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer $token")
                .build()

            Log.d(TAG, "WS URL: ${request.url}")

            runCatching { webSocket?.close(1000, "reconnect") }
            webSocket = null

            webSocket = client.newWebSocket(request, listener)
        }
    }

    private val listener = object : WebSocketListener() {

        override fun onOpen(ws: WebSocket, response: Response) {
            Log.d(TAG, "✅ WebSocket 연결 성공 (${response.code})")
            mainScope.launch {
                _connectionState.value = SessionConnectionState.Connected
            }
        }

        override fun onMessage(ws: WebSocket, text: String) {
            logLong("========== 텍스트 메시지 수신 ==========\n$text")
            parseScope.launch { handleMessage(text) }
        }

        override fun onMessage(ws: WebSocket, bytes: ByteString) {
            Log.d(TAG, "========== 바이너리 메시지 수신 ==========")
            Log.d(TAG, "bytes: ${bytes.size}")

            val text = runCatching { bytes.utf8() }.getOrNull()
            if (text.isNullOrEmpty()) return
            logLong(text)
            parseScope.launch { handleMessage(text) }
        }

        override fun onClosing(ws: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket 닫히는 중: $code, $reason")
            mainScope.launch { _connectionState.value = SessionConnectionState.Disconnected }
        }

        override fun onClosed(ws: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket 종료: $code, $reason")
            mainScope.launch { _connectionState.value = SessionConnectionState.Disconnected }
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "❌ WebSocket 실패: ${t::class.java.simpleName} / ${t.message}")
            if (response != null) {
                Log.e(TAG, "Response: ${response.code} ${response.message}")
                val body = runCatching { response.body?.string() }.getOrNull()
                if (!body.isNullOrEmpty()) Log.e(TAG, "Body: $body")
            }
            t.printStackTrace()

            val msg = t.message ?: "연결 실패"
            mainScope.launch {
                _connectionState.value = if (manualClose) {
                    SessionConnectionState.Disconnected
                } else {
                    SessionConnectionState.Error(msg)
                }
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val obj = json.parseToJsonElement(text).jsonObject
            val type = obj["type"]?.jsonPrimitive?.content

            when (type) {
                "ready" -> {
                    val sid = obj["sessionId"]?.jsonPrimitive?.content
                    Log.d(TAG, "✅ [READY] $sid")
                    mainScope.launch {
                        _connectionState.value = SessionConnectionState.Ready
                        flushPreReadyAudio()
                        // Ready 상태가 되면 keyword와 hints를 모두 on으로 설정
                        sendControl("keyword.control", "on")
                        sendControl("hints.control", "on")
                        Log.d(TAG, "✅ Keyword detection과 Resource hints를 모두 활성화했습니다")
                    }
                }

                "partial" -> {
                    val p = obj["text"]?.jsonPrimitive?.content
                    _partialText.value = p
                }

                "committed" -> {
                    val c = obj["text"]?.jsonPrimitive?.content
                    _partialText.value = null
                    if (!c.isNullOrEmpty()) {
                        val ok = _committedTexts.tryEmit(c)
                        if (!ok) Log.e(TAG, "❌ _committedTexts emit 실패")
                    }
                }

                "formatted" -> {
                    val f = obj["text"]?.jsonPrimitive?.content
                    if (!f.isNullOrEmpty()) {
                        val ok = _formattedTexts.tryEmit(f)
                        if (!ok) Log.e(TAG, "❌ _formattedTexts emit 실패")
                    }
                }

                "keywords" -> {
                    val arr = obj["keywords"]?.jsonArray
                    val list = arr?.mapNotNull { el ->
                        runCatching {
                            val k = el.jsonObject
                            KeywordHit(
                                name = k["name"]?.jsonPrimitive?.content ?: "",
                                description = k["description"]?.jsonPrimitive?.content ?: ""
                            )
                        }.getOrNull()
                    } ?: emptyList()

                    if (list.isNotEmpty()) {
                        val ok = _detectedKeywords.tryEmit(list)
                        if (!ok) Log.e(TAG, "❌ _detectedKeywords emit 실패")
                    }
                }

                "hints" -> {
                    val arr = obj["hints"]?.jsonArray
                    val list = arr?.mapNotNull { el ->
                        runCatching {
                            val h = el.jsonObject
                            ResourceHint(
                                resourceId = h["resourceId"]?.jsonPrimitive?.content ?: "",
                                resourceTitle = h["resourceTitle"]?.jsonPrimitive?.content ?: "",
                                hint = h["hint"]?.jsonPrimitive?.content ?: "",
                                sourceUrl = h["sourceUrl"]?.jsonPrimitive?.content
                            )
                        }.getOrNull()
                    } ?: emptyList()

                    if (list.isNotEmpty()) {
                        val ok = _resourceHints.tryEmit(list)
                        if (!ok) Log.e(TAG, "❌ _resourceHints emit 실패")
                    }
                }

                "keyword.status" -> {
                    val enabled = obj["enabled"]?.jsonPrimitive?.content?.toBoolean() ?: true
                    _keywordEnabled.value = enabled
                }

                "hints.status" -> {
                    val enabled = obj["enabled"]?.jsonPrimitive?.content?.toBoolean() ?: true
                    _hintsEnabled.value = enabled
                }

                "error" -> {
                    val errorElement = obj["error"]
                    val err = when {
                        errorElement == null -> "알 수 없는 오류"
                        errorElement is kotlinx.serialization.json.JsonPrimitive -> errorElement.content
                        errorElement is kotlinx.serialization.json.JsonObject -> {
                            val message = errorElement["message"]?.jsonPrimitive?.content
                            val code = errorElement["code"]?.jsonPrimitive?.content
                            val details = errorElement["details"]?.jsonPrimitive?.content
                            when {
                                message != null && code != null && details != null -> 
                                    "[$code] $message - $details"
                                message != null && code != null -> 
                                    "[$code] $message"
                                message != null && details != null -> 
                                    "$message - $details"
                                message != null -> message
                                code != null -> "오류 코드: $code"
                                else -> {
                                    try {
                                        Json { prettyPrint = true }.encodeToString(
                                            kotlinx.serialization.json.JsonObject.serializer(),
                                            errorElement
                                        )
                                    } catch (e: Exception) {
                                        errorElement.toString()
                                    }
                                }
                            }
                        }
                        else -> {
                            try {
                                errorElement.toString()
                            } catch (e: Exception) {
                                "오류 파싱 실패"
                            }
                        }
                    }
                    Log.e(TAG, "❌ [ERROR] $err")
                    Log.e(TAG, "원본 에러 데이터: ${try {
                        Json { prettyPrint = true }.encodeToString(
                            kotlinx.serialization.json.JsonElement.serializer(),
                            errorElement ?: obj
                        )
                    } catch (e: Exception) {
                        errorElement?.toString() ?: "null"
                    }}")
                    _connectionState.value = SessionConnectionState.Error(err)
                }

                null -> Log.w(TAG, "⚠️ type 없음: $text")
                else -> Log.w(TAG, "⚠️ 알 수 없는 type=$type / $text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 메시지 파싱 실패: ${e.message}")
            e.printStackTrace()
            logLong(text)
        }
    }

    private fun flushPreReadyAudio() {
        if (preReadyAudioQueue.isEmpty()) return
        Log.d(TAG, "READY 이전 버퍼 flush: ${preReadyAudioQueue.size}개")
        while (preReadyAudioQueue.isNotEmpty()) {
            val b64 = preReadyAudioQueue.removeFirst()
            internalSendAudio(b64, flush = true)
        }
    }

    fun sendAudio(base64Audio: String) {
        val count = sendCount.incrementAndGet()

        val st = _connectionState.value
        if (st !is SessionConnectionState.Connected && st !is SessionConnectionState.Ready) {
            if (count % SEND_LOG_EVERY == 1) {
                Log.w(TAG, "[전송 #$count] ❌ 연결 안됨. state=$st ws=$webSocket")
            }
            return
        }

        if (st is SessionConnectionState.Connected) {
            if (preReadyAudioQueue.size >= PRE_READY_QUEUE_LIMIT) preReadyAudioQueue.removeFirst()
            preReadyAudioQueue.addLast(base64Audio)

            if (count % SEND_LOG_EVERY == 1) {
                Log.d(TAG, "[전송 #$count] READY 전 버퍼링 (${preReadyAudioQueue.size}/$PRE_READY_QUEUE_LIMIT)")
            }
            return
        }

        internalSendAudio(base64Audio, flush = false)
    }

    private fun internalSendAudio(base64Audio: String, flush: Boolean) {
        val ws = webSocket
        if (ws == null) {
            Log.e(TAG, "❌ ws=null (flush=$flush)")
            return
        }

        runCatching {
            val msg = json.encodeToString(AudioMessage(audio = base64Audio))
            val ok = ws.send(msg)

            val c = sendCount.get()
            if (flush || c % SEND_LOG_EVERY == 1) {
                Log.d(TAG, "오디오 전송 #$c / ok=$ok / flush=$flush / b64len=${base64Audio.length}")
            }

            if (!ok) Log.e(TAG, "❌ 전송 실패 ws=$ws")
        }.onFailure { e ->
            Log.e(TAG, "❌ 전송 예외: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setKeywordEnabled(enabled: Boolean) {
        sendControl("keyword.control", if (enabled) "on" else "off")
    }

    fun setHintsEnabled(enabled: Boolean) {
        sendControl("hints.control", if (enabled) "on" else "off")
    }

    private fun sendControl(action: String, data: String) {
        val ws = webSocket
        if (ws == null) {
            Log.w(TAG, "제어 전송 불가 ws=null action=$action")
            return
        }

        runCatching {
            val message = """{"action":"$action","data":"$data"}"""
            val ok = ws.send(message)
            Log.d(TAG, "제어 전송 action=$action data=$data ok=$ok")
        }.onFailure { e ->
            Log.e(TAG, "❌ 제어 전송 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    fun disconnect() {
        Log.d(TAG, "========== WebSocket 연결 해제 ==========")
        manualClose = true

        runCatching { webSocket?.close(1000, "사용자 종료") }
        webSocket = null

        _connectionState.value = SessionConnectionState.Disconnected
        _partialText.value = null
        _sessionId.value = null
        preReadyAudioQueue.clear()
        sendCount.set(0)
    }

    fun release() {
        disconnect()

        runCatching { mainScope.cancel() }
        runCatching { ioScope.cancel() }
        runCatching { parseScope.cancel() }

        runCatching { client.dispatcher.executorService.shutdown() }
        runCatching { client.connectionPool.evictAll() }
    }

    private fun logLong(text: String) {
        if (text.length <= LOG_CHUNK) {
            Log.d(TAG, text)
            return
        }
        var offset = 0
        var chunk = 1
        while (offset < text.length) {
            val end = minOf(offset + LOG_CHUNK, text.length)
            Log.d(TAG, "[청크 $chunk] ${text.substring(offset, end)}")
            offset = end
            chunk++
        }
    }
}

sealed class SessionConnectionState {
    data object Disconnected : SessionConnectionState()
    data object Connecting : SessionConnectionState()
    data object Connected : SessionConnectionState()
    data object Ready : SessionConnectionState() // ready 메시지 수신 후
    data class Error(val message: String) : SessionConnectionState()
}

@Serializable
data class AudioMessage(val audio: String)

data class KeywordHit(
    val name: String,
    val description: String
)

data class ResourceHint(
    val resourceId: String,
    val resourceTitle: String,
    val hint: String,
    val sourceUrl: String?
)
