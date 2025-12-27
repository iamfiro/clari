package com.iamfiro.clari.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 인증이 필요 없는 경로 확인
        val path = originalRequest.url.encodedPath
        if (isPublicPath(path)) {
            return chain.proceed(originalRequest)
        }
        
        // 토큰 가져오기 (blocking)
        val token = tokenManager.getAccessTokenBlocking()
        
        // 토큰이 없으면 원본 요청 진행
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Authorization 헤더 추가
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
    
    private fun isPublicPath(path: String): Boolean {
        return path.startsWith("/auth/")
    }
}



