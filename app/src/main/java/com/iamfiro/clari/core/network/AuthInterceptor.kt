package com.iamfiro.clari.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val path = originalRequest.url.encodedPath
        if (isPublicPath(path)) {
            return chain.proceed(originalRequest)
        }
        
        val token = tokenManager.getAccessTokenBlocking()
        
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
    
    private fun isPublicPath(path: String): Boolean {
        return path == "/auth/google" || path == "/api/auth/google"
    }
}



