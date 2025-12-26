package com.iamfiro.clari.core.network

import android.content.Context
import android.util.Log
import com.iamfiro.clari.core.config.ApiConfig
import com.iamfiro.clari.core.network.api.AuthApi
import com.iamfiro.clari.core.network.api.ExternalResourceApi
import com.iamfiro.clari.core.network.api.KeywordPackApi
import com.iamfiro.clari.core.network.api.NoteApi
import com.iamfiro.clari.core.network.api.RecordingApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private const val TAG = "ApiClient"
    
    private var retrofit: Retrofit? = null
    private var tokenManager: TokenManager? = null
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    fun initialize(context: Context) {
        if (retrofit != null) {
            Log.d(TAG, "ApiClient 이미 초기화됨")
            return
        }
        
        Log.d(TAG, "ApiClient 초기화 시작 - BASE_URL: ${ApiConfig.BASE_URL}")
        tokenManager = TokenManager.getInstance(context)
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager!!))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val contentType = "application/json".toMediaType()
        
        retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        
        Log.d(TAG, "ApiClient 초기화 완료 - BASE_URL: ${ApiConfig.BASE_URL}")
    }
    
    fun getTokenManager(): TokenManager {
        return tokenManager ?: throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
    }
    
    private fun getRetrofit(): Retrofit {
        return retrofit ?: throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
    }
    
    // API Services
    val authApi: AuthApi by lazy {
        getRetrofit().create(AuthApi::class.java)
    }
    
    val noteApi: NoteApi by lazy {
        getRetrofit().create(NoteApi::class.java)
    }
    
    val recordingApi: RecordingApi by lazy {
        getRetrofit().create(RecordingApi::class.java)
    }
    
    val keywordPackApi: KeywordPackApi by lazy {
        getRetrofit().create(KeywordPackApi::class.java)
    }
    
    val externalResourceApi: ExternalResourceApi by lazy {
        getRetrofit().create(ExternalResourceApi::class.java)
    }
}

