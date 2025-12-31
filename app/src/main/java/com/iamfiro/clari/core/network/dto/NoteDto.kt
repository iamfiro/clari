package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotesResponse(
    val notes: List<NoteListItemDto>
)

@Serializable
data class NoteListItemDto(
    val id: String,
    val title: String,
    val durationInSeconds: Int,
    val createdAt: String,
    val updatedAt: String,
    val lastUpdated: String
)

@Serializable
data class NoteResponse(
    val note: NoteDto
)

@Serializable
data class NoteDto(
    val id: String,
    val title: String,
    val content: String? = null,
    val aiSummary: String? = null,
    val speakers: List<SpeakerDto>? = null,
    val durationInSeconds: Int,
    val isPublic: Boolean,
    val recordingUrl: String? = null,
    val recordingStatus: String,
    val createdAt: String,
    val updatedAt: String,
    val lastUpdated: String,
    val authorId: String,
    val keywordPackIds: List<String> = emptyList(),
    val externalResourceIds: List<String> = emptyList()
)

@Serializable
data class SpeakerDto(
    val speaker_id: String,
    val speaker_name: String
)

@Serializable
data class UpdateNoteRequest(
    val title: String? = null,
    val speakers: List<SpeakerDto>? = null
)

@Serializable
data class TranscriptContent(
    val language_code: String? = null,
    val language_probability: Double? = null,
    val text: String? = null,
    val formatted_text: String? = null,
    val words: List<TranscriptWord> = emptyList(),
    val duration_seconds: Int? = null,
    val sample_rate: Int? = null,
    val transcribed_at: String? = null,
    val segments: List<TranscriptSegment> = emptyList()
)

@Serializable
data class TranscriptWord(
    val text: String,
    val start: Double,
    val end: Double,
    val type: String,
    val speaker_id: String,
    val logprob: Double = 0.0
)

@Serializable
data class TranscriptSegment(
    val speaker_id: String,
    val start: Double,
    val end: Double,
    val text: String
)

@Serializable
data class AiExplanationRequest(
    val sentence: String
)

@Serializable
data class AiExplanationResponse(
    val sentence: String,
    val explanation: String,
    val context: AiExplanationContext? = null,
    val generatedAt: String? = null
)

@Serializable
data class AiExplanationContext(
    val userRole: String? = null,
    val noteTitle: String? = null,
    val transcriptLength: Int? = null
)

