package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.AddProjectWordRequest
import com.iamfiro.clari.core.network.dto.AutocompleteRequest
import com.iamfiro.clari.core.network.dto.AutocompleteResponse
import com.iamfiro.clari.core.network.dto.AutofillRequest
import com.iamfiro.clari.core.network.dto.AutofillResponse
import com.iamfiro.clari.core.network.dto.CreateKeywordPackRequest
import com.iamfiro.clari.core.network.dto.ProjectPackResponse
import com.iamfiro.clari.core.network.dto.ProjectPacksResponse
import com.iamfiro.clari.core.network.dto.MessageResponse
import com.iamfiro.clari.core.network.dto.UpdateProjectRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectAPI {
    
    @GET("keywordpacks")
    suspend fun getProjects(@Query("limit") limit: Int = 50): ProjectPacksResponse
    
    @GET("keywordpacks/{id}")
    suspend fun getProject(@Path("id") id: String): ProjectPackResponse
    
    @POST("keywordpacks")
    suspend fun createProject(@Body request: CreateKeywordPackRequest): ProjectPackResponse
    
    @POST("keywordpacks/{id}/keywords")
    suspend fun addProjectWord(
        @Path("id") id: String,
        @Body request: AddProjectWordRequest
    ): ProjectPackResponse
    
    @PATCH("keywordpacks/{id}")
    suspend fun updateProject(
        @Path("id") id: String,
        @Body request: UpdateProjectRequest
    ): ProjectPackResponse
    
    @DELETE("keywordpacks/{id}")
    suspend fun deleteProject(@Path("id") id: String): MessageResponse

    @POST("keywordpacks/ai/autocomplete")
    suspend fun generateWordDescription(@Body request: AutocompleteRequest): AutocompleteResponse
    
    @POST("keywordpacks/ai/autofill")
    suspend fun generateAutofill(@Body request: AutofillRequest): AutofillResponse
}