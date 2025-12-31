package com.iamfiro.clari.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val profileUrl: String? = null,
    val role: String? = null,
    val isActive: Boolean = false,
    val savedKeywordPackIds: List<String> = emptyList()
)

@Serializable
data class RegisterContinueRequest(
    val role: String,
    val name: String? = null
)

@Serializable
data class RegisterContinueResponse(
    val user: UserDto
)

@Serializable
data class MeResponse(
    val user: UserDto
)

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class MessageResponse(
    val message: String
)



