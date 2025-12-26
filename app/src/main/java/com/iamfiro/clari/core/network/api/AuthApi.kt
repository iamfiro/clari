package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.AuthResponse
import com.iamfiro.clari.core.network.dto.GoogleAuthRequest
import com.iamfiro.clari.core.network.dto.MeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): AuthResponse
    
    @GET("me")
    suspend fun getMe(): MeResponse
}


