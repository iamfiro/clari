package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class KeywordPacksResponse(
    val packs: List<KeywordPackDto>
)

@Serializable
data class KeywordPackResponse(
    val pack: KeywordPackDto
)

@Serializable
data class KeywordPackDto(
    val id: String,
    val name: String,
    val keywords: List<KeywordDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val isPublic: Boolean,
    val previewImageUrl: String? = null
)

@Serializable
data class KeywordDto(
    val name: String,
    val description: String
)

@Serializable
data class CreateKeywordPackRequest(
    val name: String,
    val keywords: List<KeywordDto> = emptyList(),
    val isPublic: Boolean = false
)

@Serializable
data class AddKeywordRequest(
    val name: String,
    val description: String
)

@Serializable
data class UpdateKeywordPackRequest(
    val name: String? = null,
    val keywords: List<KeywordDto>? = null,
    val isPublic: Boolean? = null,
    val previewImageUrl: String? = null
)

// AI Features
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
    val keywords: List<KeywordDto>
)

