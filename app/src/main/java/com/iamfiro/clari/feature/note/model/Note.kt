package com.iamfiro.clari.feature.note.model

import java.time.LocalDateTime

enum class NoteType {
    NOT_READY,
    READY
}

data class Note(
    val id: String? = null,
    val type: NoteType = NoteType.NOT_READY,
    val name: String,
    val duration: Long,
    val createdAt: LocalDateTime,
    val recordedAtText: String? = null,
    val aiSummary: AiSummary? = null,
    val transcripts: List<TranscriptLine>? = null,
)

data class AiSummary(
    val title: String,
    val content: String,
)

data class Speaker(
    val id: Int,
    val label: String = "참석자 $id",
)

data class TranscriptLine(
    val speaker: Speaker,
    val timeSec: Int,
    val text: String,
)
