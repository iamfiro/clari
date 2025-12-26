package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ExternalResourcesResponse(
    val resources: List<ExternalResourceListItemDto>
)

@Serializable
data class ExternalResourceListItemDto(
    val id: String,
    val url: String,
    val displayUrl: String,
    val title: String,
    val logoUrl: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ExternalResourceResponse(
    val resource: ExternalResourceDto
)

@Serializable
data class ExternalResourceDto(
    val id: String,
    val url: String,
    val displayUrl: String,
    val title: String,
    val logoUrl: String? = null,
    val scrapedContent: String? = null,
    val metadata: JsonObject? = null,
    val createdAt: String,
    val updatedAt: String,
    val authorId: String
)

@Serializable
data class CreateExternalResourceRequest(
    val url: String
)

@Serializable
data class UpdateExternalResourceRequest(
    val title: String
)


