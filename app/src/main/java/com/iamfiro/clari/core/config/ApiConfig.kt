package com.iamfiro.clari.core.config

object ApiConfig {
    // 서버 기본 설정
    private const val SERVER_IP = "172.30.2.38"
    private const val SERVER_PORT = "8000"
    
    // WebSocket 엔드포인트
    object WebSocket {
        const val BASE_URL = "ws://$SERVER_IP:$SERVER_PORT"
        const val STT_ENDPOINT = "$BASE_URL/ws/stt"
    }
}
