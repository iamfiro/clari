package com.iamfiro.clari.core.config

object ApiConfig {
    private const val HOST = "clari-api.thnos.app"

    const val BASE_URL = "https://$HOST/"

    private const val WS_BASE_URL = "wss://$HOST"

    object WebSocket {
        const val STT_ENDPOINT = "$WS_BASE_URL/ws/stt"

        fun recordingSessionEndpoint(sessionId: String, token: String): String {
            return "$WS_BASE_URL/notes/session/$sessionId?token=$token"
        }
    }
}
