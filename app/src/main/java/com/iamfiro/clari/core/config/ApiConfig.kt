package com.iamfiro.clari.core.config

/**
 * API 설정 관리
 * WebSocket 및 REST API 엔드포인트 정의
 */
object ApiConfig {
    // 서버 기본 설정
    private const val SERVER_IP = "172.30.2.38"  // TODO: 실제 서버 IP로 변경
    private const val SERVER_PORT = "8000"
    
    // WebSocket 엔드포인트
    object WebSocket {
        const val BASE_URL = "ws://$SERVER_IP:$SERVER_PORT"
        const val STT_ENDPOINT = "$BASE_URL/ws/stt"
    }
}

