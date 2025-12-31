package com.iamfiro.clari.core.network.api

import com.iamfiro.clari.core.network.dto.AuthResponse
import com.iamfiro.clari.core.network.dto.GoogleAuthRequest
import com.iamfiro.clari.core.network.dto.MeResponse
import com.iamfiro.clari.core.network.dto.RegisterContinueRequest
import com.iamfiro.clari.core.network.dto.RegisterContinueResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): AuthResponse
    
    @POST("auth/register/continue")
    suspend fun registerContinue(@Body request: RegisterContinueRequest): RegisterContinueResponse
    
    @GET("me")
    suspend fun getMe(): MeResponse
}



