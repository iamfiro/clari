package com.iamfiro.clari.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("/api/fcm/token")
    suspend fun registerToken(@Body request: RegisterFcmTokenRequest): RegisterFcmTokenResponse
}

@Serializable
data class RegisterFcmTokenRequest(
    val token: String,
    val deviceId: String? = null,
    val platform: String = "android"
)

@Serializable
data class RegisterFcmTokenResponse(
    val success: Boolean,
    val message: String? = null
)
