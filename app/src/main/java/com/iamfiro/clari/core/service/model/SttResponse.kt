package com.iamfiro.clari.core.service.model

import kotlinx.serialization.Serializable

/**
 * STT WebSocket 응답 모델
 */
@Serializable
data class SttResponse(
    val type: String,           // "partial", "committed", "formatted"
    val text: String,           // 전체 텍스트
    val chunks: List<String>    // UI 표시용 청크 (최대 3줄)
)

/**
 * STT 응답 타입
 */
enum class SttResponseType(val value: String) {
    PARTIAL("partial"),         // 실시간 인식 중
    COMMITTED("committed"),     // 최종 인식 완료
    FORMATTED("formatted");     // AI 교정 완료
    
    companion object {
        fun fromString(value: String): SttResponseType? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * UI에 표시할 텍스트 아이템
 * committed와 formatted를 하나의 아이템으로 관리
 */
data class TranscriptItem(
    val id: Int,                        // 고유 ID (순서)
    val committedText: String,          // committed 원본 텍스트
    val committedChunks: List<String>,  // committed 청크
    val formattedText: String? = null,  // formatted 텍스트 (null이면 아직 안옴)
    val formattedChunks: List<String>? = null,  // formatted 청크
    val isFormatted: Boolean = false    // formatted 처리가 완료되었는지
) {
    // 현재 표시할 텍스트 (formatted 있으면 formatted, 없으면 committed)
    val displayText: String
        get() = formattedText ?: committedText
    
    // 현재 표시할 청크
    val displayChunks: List<String>
        get() = formattedChunks ?: committedChunks
}

/**
 * 클라이언트 → 서버 오디오 메시지
 */
@Serializable
data class AudioMessage(
    val audio: String  // Base64 인코딩된 PCM 16kHz Mono 오디오
)
