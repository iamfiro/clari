package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.AddKeywordRequest
import com.iamfiro.clari.core.network.dto.AutocompleteRequest
import com.iamfiro.clari.core.network.dto.AutocompleteResponse
import com.iamfiro.clari.core.network.dto.AutofillRequest
import com.iamfiro.clari.core.network.dto.AutofillResponse
import com.iamfiro.clari.core.network.dto.CreateKeywordPackRequest
import com.iamfiro.clari.core.network.dto.KeywordPackResponse
import com.iamfiro.clari.core.network.dto.KeywordPacksResponse
import com.iamfiro.clari.core.network.dto.MessageResponse
import com.iamfiro.clari.core.network.dto.UpdateKeywordPackRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KeywordPackApi {
    
    @GET("keywordpacks")
    suspend fun getKeywordPacks(@Query("limit") limit: Int = 50): KeywordPacksResponse
    
    @GET("keywordpacks/{id}")
    suspend fun getKeywordPack(@Path("id") id: String): KeywordPackResponse
    
    @POST("keywordpacks")
    suspend fun createKeywordPack(@Body request: CreateKeywordPackRequest): KeywordPackResponse
    
    @POST("keywordpacks/{id}/keywords")
    suspend fun addKeyword(
        @Path("id") id: String,
        @Body request: AddKeywordRequest
    ): KeywordPackResponse
    
    @PATCH("keywordpacks/{id}")
    suspend fun updateKeywordPack(
        @Path("id") id: String,
        @Body request: UpdateKeywordPackRequest
    ): KeywordPackResponse
    
    @DELETE("keywordpacks/{id}")
    suspend fun deleteKeywordPack(@Path("id") id: String): MessageResponse
    
    // AI Features
    @POST("keywordpacks/ai/autocomplete")
    suspend fun aiAutocomplete(@Body request: AutocompleteRequest): AutocompleteResponse
    
    @POST("keywordpacks/ai/autofill")
    suspend fun aiAutofill(@Body request: AutofillRequest): AutofillResponse
}

