package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectPacksResponse(
    val packs: List<ProjectDto>
)

@Serializable
data class ProjectPackResponse(
    val pack: ProjectDto
)

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    @SerialName("keywords")
    val words: List<ProjectWordDTO> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val isPublic: Boolean,
    val previewImageUrl: String? = null,
    val authorId: String? = null,
    val isOwned: Boolean = true,
    val isSaved: Boolean = false
)

@Serializable
data class ProjectWordDTO(
    val name: String,
    val description: String,
    val koreanPronunciation: String? = null,
    val synonyms: List<String>? = null
)

@Serializable
data class CreateKeywordPackRequest(
    val name: String,
    val keywords: List<ProjectWordDTO> = emptyList(),
    val isPublic: Boolean = false
)

@Serializable
data class AddProjectWordRequest(
    val name: String,
    val description: String,
    val koreanPronunciation: String? = null,
    val synonyms: List<String>? = null
)

@Serializable
data class UpdateProjectRequest(
    val name: String? = null,
    val keywords: List<ProjectWordDTO>? = null,
    val isPublic: Boolean? = null,
    val previewImageUrl: String? = null
)

@Serializable
data class AutocompleteRequest(
    val name: String
)

@Serializable
data class AutocompleteResponse(
    val suggestions: List<String>
)

@Serializable
data class AutofillRequest(
    val query: String,
    val count: Int = 50
)

@Serializable
data class AutofillResponse(
    val keywords: List<ProjectWordDTO>,
    val stats: AutofillStats? = null
)

@Serializable
data class AutofillStats(
    val perplexityTime: Long? = null,
    val gptTime: Long? = null,
    val pronunciationTime: Long? = null,
    val synonymTime: Long? = null,
    val totalTime: Long? = null,
    val requestedCount: Int? = null,
    val actualCount: Int? = null,
    val withPronunciation: Int? = null,
    val withSynonyms: Int? = null
)

@Serializable
data class CloudSaveResponse(
    val message: String,
    val pack: CloudSavePackInfo? = null
)

@Serializable
data class CloudSavePackInfo(
    val id: String,
    val name: String,
    val isOwned: Boolean,
    val isSaved: Boolean
)


