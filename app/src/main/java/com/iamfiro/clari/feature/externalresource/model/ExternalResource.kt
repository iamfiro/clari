package com.iamfiro.clari.feature.externalresource.model

data class ExternalResource(
    val id: String,
    val url: String,
    val displayUrl: String,
    val title: String,
    val logoUrl: String? = null,
    val scrapedContent: String? = null,
    val createdAt: String,
    val updatedAt: String
)



