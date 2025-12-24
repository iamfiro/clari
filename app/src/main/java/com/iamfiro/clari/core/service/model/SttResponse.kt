package com.iamfiro.clari.core.service.model

import kotlinx.serialization.Serializable

@Serializable
data class SttResponse(
    val type: String,           // "partial", "committed", "formatted"
    val text: String,
    val chunks: List<String>
)

enum class SttResponseType(val value: String) {
    PARTIAL("partial"),
    COMMITTED("committed"),
    FORMATTED("formatted");
    
    companion object {
        fun fromString(value: String): SttResponseType? {
            return entries.find { it.value == value }
        }
    }
}

data class TranscriptItem(
    val id: Int,
    val committedText: String,
    val committedChunks: List<String>,
    val formattedText: String? = null,
    val formattedChunks: List<String>? = null,
    val isFormatted: Boolean = false
) {
    val displayText: String
        get() = formattedText ?: committedText
    
    // 현재 표시할 청크
    val displayChunks: List<String>
        get() = formattedChunks ?: committedChunks
}

@Serializable
data class AudioMessage(
    val audio: String
)
