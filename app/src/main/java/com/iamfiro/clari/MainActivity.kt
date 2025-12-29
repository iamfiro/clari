package com.iamfiro.clari

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.service.FcmTokenManager
import com.skills.app.core.ui.theme.ClariTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // API 클라이언트 초기화
        ApiClient.initialize(applicationContext)
        
        // FCM 토큰 가져오기 및 서버 전송
        initializeFcm()
        
        enableEdgeToEdge()

        setContent {
            ClariTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AppRoot()
                }
            }
        }
    }
    
    private fun initializeFcm() {
        lifecycleScope.launch {
            FcmTokenManager.getToken()
                .onSuccess { token ->
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "MainActivity - FCM 토큰 초기화 성공")
                    Log.d(TAG, "토큰: $token")
                    Log.d(TAG, "========================================")
                    sendTokenToServer(token)
                }
                .onFailure { e ->
                    Log.e(TAG, "========================================")
                    Log.e(TAG, "MainActivity - FCM 토큰 초기화 실패", e)
                    Log.e(TAG, "========================================")
                }
        }
    }
    
    private fun sendTokenToServer(token: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "서버에 FCM 토큰 전송 시도...")
                val response = ApiClient.fcmApi.registerToken(
                    com.iamfiro.clari.core.network.api.RegisterFcmTokenRequest(
                        token = token
                    )
                )
                if (response.success) {
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "서버에 FCM 토큰 전송 성공")
                    Log.d(TAG, "응답: ${response.message}")
                    Log.d(TAG, "========================================")
                } else {
                    Log.w(TAG, "========================================")
                    Log.w(TAG, "서버에 FCM 토큰 전송 실패")
                    Log.w(TAG, "메시지: ${response.message}")
                    Log.w(TAG, "========================================")
                }
            } catch (e: Exception) {
                Log.e(TAG, "========================================")
                Log.e(TAG, "서버에 FCM 토큰 전송 중 오류", e)
                Log.e(TAG, "========================================")
            }
        }
    }
}
