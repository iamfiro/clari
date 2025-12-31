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
    
    val sessionToken: Flow<String?> = tokenDao.getToken().map { it?.sessionToken }
    
    suspend fun getSessionToken(): String? {
        return tokenDao.getTokenOnce()?.sessionToken
    }
    
    fun getSessionTokenBlocking(): String? {
        return runBlocking { getSessionToken() }
    }
    
    val accessToken: Flow<String?> = sessionToken
    
    suspend fun getAccessToken(): String? {
        return getSessionToken()
    }
    
    fun getAccessTokenBlocking(): String? {
        return getSessionTokenBlocking()
    }
    
    suspend fun saveAccessToken(token: String) {
        saveSessionToken(token, null, null, null, null, null, false)
    }
    
    suspend fun saveSessionToken(
        sessionToken: String,
        userId: String?,
        email: String?,
        name: String?,
        profileUrl: String?,
        role: String? = null,
        isActive: Boolean = false
    ) {
        val tokenEntity = TokenEntity(
            id = 1,
            sessionToken = sessionToken,
            userId = userId,
            userEmail = email,
            userName = name,
            userProfileUrl = profileUrl,
            userRole = role,
            isActive = isActive
        )
        tokenDao.saveToken(tokenEntity)
    }
    
    suspend fun saveUserInfo(userId: String, email: String, name: String?, profileUrl: String?) {
        val currentToken = getSessionToken() ?: return
        val current = tokenDao.getTokenOnce()
        saveSessionToken(currentToken, userId, email, name, profileUrl, current?.userRole, current?.isActive ?: false)
    }
    
    suspend fun updateUserActivation(role: String, name: String?, isActive: Boolean) {
        val current = tokenDao.getTokenOnce() ?: return
        val tokenEntity = TokenEntity(
            id = 1,
            sessionToken = current.sessionToken,
            userId = current.userId,
            userEmail = current.userEmail,
            userName = name ?: current.userName,
            userProfileUrl = current.userProfileUrl,
            userRole = role,
            isActive = isActive
        )
        tokenDao.saveToken(tokenEntity)
    }
    
    val userId: Flow<String?> = tokenDao.getToken().map { it?.userId }
    
    val userEmail: Flow<String?> = tokenDao.getToken().map { it?.userEmail }
    
    val userName: Flow<String?> = tokenDao.getToken().map { it?.userName }
    
    val userProfileUrl: Flow<String?> = tokenDao.getToken().map { it?.userProfileUrl }
    
    val userRole: Flow<String?> = tokenDao.getToken().map { it?.userRole }
    
    val isActive: Flow<Boolean> = tokenDao.getToken().map { it?.isActive ?: false }
    
    suspend fun getUserId(): String? {
        return tokenDao.getTokenOnce()?.userId
    }
    
    suspend fun getIsActive(): Boolean {
        return tokenDao.getTokenOnce()?.isActive ?: false
    }
    
    suspend fun clearAll() {
        tokenDao.deleteToken()
    }
    
    suspend fun clearAccessToken() {
        tokenDao.deleteToken()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return getSessionToken() != null
    }
    
    val isLoggedInFlow: Flow<Boolean> = sessionToken.map { it != null }
}

