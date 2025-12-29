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
    val formattedText: String? = null,
    val words: List<TranscriptWord>? = null,
    val keywordPackIds: List<String> = emptyList(),
)

data class AiSummary(
    val title: String,
    val content: String,
)

data class Speaker(
    val id: String,
    val label: String = "참석자",
)

data class TranscriptLine(
    val speaker: Speaker,
    val timeSec: Int,
    val text: String,
    val startMs: Long = (timeSec * 1000).toLong(),
    val endMs: Long = startMs,
)
data class TranscriptWord(
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val speakerId: String,
    val isSpacing: Boolean = false,
)
