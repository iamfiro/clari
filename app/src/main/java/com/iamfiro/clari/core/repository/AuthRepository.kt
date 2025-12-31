package com.iamfiro.clari.core.repository

import android.util.Log
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.TokenManager
import com.iamfiro.clari.core.network.dto.AuthResponse
import com.iamfiro.clari.core.network.dto.GoogleAuthRequest
import com.iamfiro.clari.core.network.dto.RegisterContinueRequest
import com.iamfiro.clari.core.network.dto.UserDto
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val tokenManager: TokenManager
) {
    
    companion object {
        private const val TAG = "AuthRepository"
        
        @Volatile
        private var INSTANCE: AuthRepository? = null
        
        fun getInstance(tokenManager: TokenManager): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(tokenManager).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "loginWithGoogle 호출 - idToken 길이: ${idToken.length}")
            val request = GoogleAuthRequest(idToken = idToken)
            Log.d(TAG, "API 호출 시작 - 엔드포인트: auth/google")
            val response = ApiClient.authApi.loginWithGoogle(request)
            Log.d(TAG, "API 호출 성공 - 사용자 이메일: ${response.user.email}, isActive: ${response.user.isActive}")
            
            tokenManager.saveSessionToken(
                sessionToken = response.accessToken,
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                profileUrl = response.user.profileUrl,
                role = response.user.role,
                isActive = response.user.isActive
            )
            Log.d(TAG, "세션 토큰 및 유저 정보 Room에 저장 완료")
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "loginWithGoogle 실패 - 타입: ${e.javaClass.simpleName}, 메시지: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun registerContinue(role: String, name: String? = null): Result<UserDto> {
        return try {
            Log.d(TAG, "registerContinue 호출 - role: $role, name: $name")
            val request = RegisterContinueRequest(role = role, name = name)
            val response = ApiClient.authApi.registerContinue(request)
            Log.d(TAG, "registerContinue 성공 - isActive: ${response.user.isActive}")
            
            tokenManager.updateUserActivation(
                role = response.user.role ?: role,
                name = response.user.name,
                isActive = response.user.isActive
            )
            
            Result.success(response.user)
        } catch (e: Exception) {
            Log.e(TAG, "registerContinue 실패 - 타입: ${e.javaClass.simpleName}, 메시지: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMe(): Result<UserDto> {
        return try {
            val response = ApiClient.authApi.getMe()
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        tokenManager.clearAll()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    suspend fun isActive(): Boolean {
        return tokenManager.getIsActive()
    }
    
    val isLoggedInFlow: Flow<Boolean> = tokenManager.isLoggedInFlow
    
    val isActiveFlow: Flow<Boolean> = tokenManager.isActive
    
    suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
    
    val userId: Flow<String?> = tokenManager.userId
    val userEmail: Flow<String?> = tokenManager.userEmail
    val userName: Flow<String?> = tokenManager.userName
    val userProfileUrl: Flow<String?> = tokenManager.userProfileUrl
    val userRole: Flow<String?> = tokenManager.userRole
}

