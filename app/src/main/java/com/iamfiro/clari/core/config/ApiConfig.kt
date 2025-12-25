package com.iamfiro.clari.core.config

object ApiConfig {
    // 서버 기본 설정
    private const val BASE_URL = "https://vsnet-lincoln-breaks-implications.trycloudflare.com"
    
    // WebSocket 엔드포인트
    object WebSocket {
        const val STT_ENDPOINT = "wss://vsnet-lincoln-breaks-implications.trycloudflare.com/ws/stt"
    }
}
