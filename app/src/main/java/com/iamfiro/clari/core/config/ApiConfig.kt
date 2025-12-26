package com.iamfiro.clari.core.config

object ApiConfig {
    // 서버 기본 설정
    private const val HOST = "pin-renaissance-meters-britannica.trycloudflare.com"
    
    // HTTP API
    const val BASE_URL = "https://$HOST/"
    
    // WebSocket 기본 URL
    private const val WS_BASE_URL = "wss://$HOST"
    
    // WebSocket 엔드포인트
    object WebSocket {
        // STT 전용 WebSocket (녹음 세션 없이 단독 STT)
        const val STT_ENDPOINT = "$WS_BASE_URL/ws/stt"
        
        // 녹음 세션 WebSocket - sessionId와 token을 파라미터로 받음
        fun recordingSessionEndpoint(sessionId: String, token: String): String {
            return "$WS_BASE_URL/notes/session/$sessionId?token=$token"
        }
    }
}
