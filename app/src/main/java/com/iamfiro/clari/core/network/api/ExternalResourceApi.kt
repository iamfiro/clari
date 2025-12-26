package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.CreateExternalResourceRequest
import com.iamfiro.clari.core.network.dto.ExternalResourceResponse
import com.iamfiro.clari.core.network.dto.ExternalResourcesResponse
import com.iamfiro.clari.core.network.dto.MessageResponse
import com.iamfiro.clari.core.network.dto.UpdateExternalResourceRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExternalResourceApi {
    
    @GET("externalresources")
    suspend fun getResources(@Query("limit") limit: Int = 50): ExternalResourcesResponse
    
    @GET("externalresources/{id}")
    suspend fun getResource(@Path("id") id: String): ExternalResourceResponse
    
    @POST("externalresources")
    suspend fun createResource(@Body request: CreateExternalResourceRequest): ExternalResourceResponse
    
    @PATCH("externalresources/{id}")
    suspend fun updateResource(
        @Path("id") id: String,
        @Body request: UpdateExternalResourceRequest
    ): ExternalResourceResponse
    
    @DELETE("externalresources/{id}")
    suspend fun deleteResource(@Path("id") id: String): MessageResponse
}

