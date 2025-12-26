package com.iamfiro.clari.core.network

import android.content.Context
import com.iamfiro.clari.core.database.AppDatabase
import com.iamfiro.clari.core.database.dao.TokenDao
import com.iamfiro.clari.core.database.entity.TokenEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class TokenManager(context: Context) {
    
    private val tokenDao: TokenDao = AppDatabase.getInstance(context).tokenDao()
    
    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null
        
        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Session Token (from Room)
    val sessionToken: Flow<String?> = tokenDao.getToken().map { it?.sessionToken }
    
    suspend fun getSessionToken(): String? {
        return tokenDao.getTokenOnce()?.sessionToken
    }
    
    fun getSessionTokenBlocking(): String? {
        return runBlocking { getSessionToken() }
    }
    
    // 기존 코드 호환성을 위한 alias
    val accessToken: Flow<String?> = sessionToken
    
    suspend fun getAccessToken(): String? {
        return getSessionToken()
    }
    
    fun getAccessTokenBlocking(): String? {
        return getSessionTokenBlocking()
    }
    
    suspend fun saveAccessToken(token: String) {
        saveSessionToken(token, null, null, null, null)
    }
    
    // 세션 토큰과 유저 정보를 함께 저장
    suspend fun saveSessionToken(
        sessionToken: String,
        userId: String?,
        email: String?,
        name: String?,
        profileUrl: String?
    ) {
        val tokenEntity = TokenEntity(
            id = 1,
            sessionToken = sessionToken,
            userId = userId,
            userEmail = email,
            userName = name,
            userProfileUrl = profileUrl
        )
        tokenDao.saveToken(tokenEntity)
    }
    
    // User Info
    suspend fun saveUserInfo(userId: String, email: String, name: String?, profileUrl: String?) {
        val currentToken = getSessionToken() ?: return
        saveSessionToken(currentToken, userId, email, name, profileUrl)
    }
    
    val userId: Flow<String?> = tokenDao.getToken().map { it?.userId }
    
    val userEmail: Flow<String?> = tokenDao.getToken().map { it?.userEmail }
    
    val userName: Flow<String?> = tokenDao.getToken().map { it?.userName }
    
    val userProfileUrl: Flow<String?> = tokenDao.getToken().map { it?.userProfileUrl }
    
    suspend fun getUserId(): String? {
        return tokenDao.getTokenOnce()?.userId
    }
    
    // 로그아웃 시 모든 데이터 삭제
    suspend fun clearAll() {
        tokenDao.deleteToken()
    }
    
    suspend fun clearAccessToken() {
        tokenDao.deleteToken()
    }
    
    // 로그인 상태 확인
    suspend fun isLoggedIn(): Boolean {
        return getSessionToken() != null
    }
    
    val isLoggedInFlow: Flow<Boolean> = sessionToken.map { it != null }
}

