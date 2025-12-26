package com.iamfiro.clari.core.repository

import com.iamfiro.clari.core.mapper.ExternalResourceMapper
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.dto.CreateExternalResourceRequest
import com.iamfiro.clari.core.network.dto.UpdateExternalResourceRequest
import com.iamfiro.clari.feature.externalresource.model.ExternalResource

class ExternalResourceRepository {
    
    companion object {
        @Volatile
        private var INSTANCE: ExternalResourceRepository? = null
        
        fun getInstance(): ExternalResourceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExternalResourceRepository().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 외부 리소스 목록 조회
     */
    suspend fun getAllResources(limit: Int = 50): Result<List<ExternalResource>> {
        return try {
            val response = ApiClient.externalResourceApi.getResources(limit = limit)
            val resources = response.resources.map { ExternalResourceMapper.fromListItemDto(it) }
            Result.success(resources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 외부 리소스 상세 조회 (스크랩 콘텐츠 포함)
     */
    suspend fun getResourceById(resourceId: String): Result<ExternalResource> {
        return try {
            val response = ApiClient.externalResourceApi.getResource(resourceId)
            val resource = ExternalResourceMapper.fromDto(response.resource)
            Result.success(resource)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 외부 리소스 생성 (URL 스크래핑)
     */
    suspend fun createResource(url: String): Result<ExternalResource> {
        return try {
            val request = CreateExternalResourceRequest(url = url)
            val response = ApiClient.externalResourceApi.createResource(request)
            val resource = ExternalResourceMapper.fromDto(response.resource)
            Result.success(resource)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 외부 리소스 제목 수정 (최대 10자)
     */
    suspend fun updateResourceTitle(resourceId: String, title: String): Result<ExternalResource> {
        return try {
            val request = UpdateExternalResourceRequest(title = title)
            val response = ApiClient.externalResourceApi.updateResource(resourceId, request)
            val resource = ExternalResourceMapper.fromDto(response.resource)
            Result.success(resource)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 외부 리소스 삭제
     */
    suspend fun deleteResource(resourceId: String): Result<Unit> {
        return try {
            ApiClient.externalResourceApi.deleteResource(resourceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


