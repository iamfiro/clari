package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequest(
    val title: String? = null,
    val languageCode: String = "ko",
    val keywordPackIds: List<String> = emptyList(),
    val externalResourceIds: List<String> = emptyList()
)

@Serializable
data class SessionResponse(
    val sessionId: String,
    val noteId: String,
    val message: String
)

@Serializable
data class StopSessionRequest(
    val sessionId: String
)

@Serializable
data class StopSessionResponse(
    val message: String,
    val recordingUrl: String? = null,
    val durationInSeconds: Int,
    val transcript: TranscriptResultDto? = null,
    val speakers: List<SpeakerResultDto>? = null
)

@Serializable
data class TranscriptResultDto(
    val text: String,
    val formatted: String? = null,
    val language: String,
    val language_probability: Double,
    val word_count: Int
)

@Serializable
data class SpeakerResultDto(
    val speaker_id: String,
    val text: String,
    val word_count: Int
)

@Serializable
data class CancelSessionRequest(
    val sessionId: String
)

@Serializable
data class RecordingUrlResponse(
    val recordingUrl: String,
    val durationInSeconds: Int
)



