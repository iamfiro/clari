package com.iamfiro.clari.core.repository

import android.util.Log
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.network.TokenManager
import com.iamfiro.clari.core.network.dto.AuthResponse
import com.iamfiro.clari.core.network.dto.GoogleAuthRequest
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
    
    /**
     * Google idToken으로 백엔드 인증
     * 성공 시 accessToken과 유저 정보를 Room에 저장
     */
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            Log.d(TAG, "loginWithGoogle 호출 - idToken 길이: ${idToken.length}")
            val request = GoogleAuthRequest(idToken = idToken)
            Log.d(TAG, "API 호출 시작 - 엔드포인트: auth/google")
            val response = ApiClient.authApi.loginWithGoogle(request)
            Log.d(TAG, "API 호출 성공 - 사용자 이메일: ${response.user.email}")
            
            // 세션 토큰과 유저 정보를 Room에 저장
            tokenManager.saveSessionToken(
                sessionToken = response.accessToken,
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                profileUrl = response.user.profileUrl
            )
            Log.d(TAG, "세션 토큰 및 유저 정보 Room에 저장 완료")
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "loginWithGoogle 실패 - 타입: ${e.javaClass.simpleName}, 메시지: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 현재 유저 정보 조회
     */
    suspend fun getMe(): Result<UserDto> {
        return try {
            val response = ApiClient.authApi.getMe()
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 로그아웃
     */
    suspend fun logout() {
        tokenManager.clearAll()
    }
    
    /**
     * 로그인 상태 확인
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * 로그인 상태 Flow
     */
    val isLoggedInFlow: Flow<Boolean> = tokenManager.isLoggedInFlow
    
    /**
     * 현재 액세스 토큰
     */
    suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
    
    /**
     * 유저 정보 Flow
     */
    val userId: Flow<String?> = tokenManager.userId
    val userEmail: Flow<String?> = tokenManager.userEmail
    val userName: Flow<String?> = tokenManager.userName
    val userProfileUrl: Flow<String?> = tokenManager.userProfileUrl
}

