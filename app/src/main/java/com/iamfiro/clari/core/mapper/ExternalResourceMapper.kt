package com.iamfiro.clari.core.mapper

import com.iamfiro.clari.core.network.dto.ExternalResourceDto
import com.iamfiro.clari.core.network.dto.ExternalResourceListItemDto
import com.iamfiro.clari.feature.externalresource.model.ExternalResource

object ExternalResourceMapper {
    
    /**
     * ExternalResourceListItemDto -> ExternalResource (리스트용)
     */
    fun fromListItemDto(dto: ExternalResourceListItemDto): ExternalResource {
        return ExternalResource(
            id = dto.id,
            url = dto.url,
            displayUrl = dto.displayUrl,
            title = dto.title,
            logoUrl = dto.logoUrl,
            scrapedContent = null, // 리스트에서는 미포함
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
    
    /**
     * ExternalResourceDto -> ExternalResource (상세용)
     */
    fun fromDto(dto: ExternalResourceDto): ExternalResource {
        return ExternalResource(
            id = dto.id,
            url = dto.url,
            displayUrl = dto.displayUrl,
            title = dto.title,
            logoUrl = dto.logoUrl,
            scrapedContent = dto.scrapedContent,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
}


