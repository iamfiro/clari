package com.iamfiro.clari.feature.note.model

import java.time.LocalDateTime

enum class NoteType {
    NOT_READY,
    READY
}

data class Note(
    val id: String,
    val type: NoteType = NoteType.NOT_READY,
    val name: String,
    val duration: Long, // milliseconds
    val createdAt: LocalDateTime,
    val recordedAtText: String? = null,
    val aiSummary: AiSummary? = null,
    val transcripts: List<TranscriptLine>? = null,
    val recordingUrl: String? = null,
    val speakers: List<Speaker> = emptyList(),
    val formattedText: String? = null, // 전체 포맷된 텍스트
)

data class AiSummary(
    val title: String,
    val content: String,
)

data class Speaker(
    val id: String,
    val label: String = "참석자",
)

/**
 * 각 단어 단위의 transcript
 * timeSec -> startMs, endMs로 변경하여 더 정밀한 타이밍 지원
 */
data class TranscriptLine(
    val speaker: Speaker,
    val timeSec: Int, // 시작 시간 (초) - 하위 호환성
    val text: String,
    val startMs: Long = (timeSec * 1000).toLong(), // 시작 시간 (밀리초)
    val endMs: Long = startMs, // 종료 시간 (밀리초)
)

/**
 * 개별 단어 정보 (하이라이트용)
 */
data class TranscriptWord(
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val speakerId: String,
    val isSpacing: Boolean = false,
)
