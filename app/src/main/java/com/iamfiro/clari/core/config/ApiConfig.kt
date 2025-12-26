package com.iamfiro.clari.core.config

object ApiConfig {
    private const val HOST = "172.30.2.38:3000"

    const val BASE_URL = "http://$HOST/"

    private const val WS_BASE_URL = "ws://$HOST"

    object WebSocket {
        const val STT_ENDPOINT = "$WS_BASE_URL/ws/stt"

        fun recordingSessionEndpoint(sessionId: String, token: String): String {
            return "$WS_BASE_URL/notes/session/$sessionId?token=$token"
        }
    }
}
