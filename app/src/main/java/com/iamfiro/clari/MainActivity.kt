package com.iamfiro.clari

import android.content.Intent
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class DeepLinkData(
    val projectId: String,
    val source: String? = null
)

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        
        private val _deepLinkFlow = MutableSharedFlow<DeepLinkData>(replay = 1)
        val deepLinkFlow = _deepLinkFlow.asSharedFlow()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ApiClient.initialize(applicationContext)
        
        initializeFcm()
        
        handleIntent(intent)
        
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
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        Log.d(TAG, "Deep link received: $uri")
        
        when {
            uri.host == "clari-deeplink.thnos.app" -> {
                val projectId = uri.lastPathSegment
                if (!projectId.isNullOrEmpty()) {
                    Log.d(TAG, "Web deep link - projectId: $projectId")
                    lifecycleScope.launch {
                        _deepLinkFlow.emit(DeepLinkData(projectId = projectId, source = "web"))
                    }
                }
            }
            uri.scheme == "clari" && uri.host == "project" -> {
                val shareId = uri.getQueryParameter("shareId")
                val source = uri.getQueryParameter("source")
                if (!shareId.isNullOrEmpty()) {
                    Log.d(TAG, "App deep link - shareId: $shareId, source: $source")
                    lifecycleScope.launch {
                        _deepLinkFlow.emit(DeepLinkData(projectId = shareId, source = source))
                    }
                }
            }
        }
    }
    
    private fun initializeFcm() {
        lifecycleScope.launch {
            FcmTokenManager.getToken()
                .onSuccess { token ->
                    Log.d(TAG, "FCM 토큰 초기화 성공: $token")
                }
                .onFailure { e ->
                    Log.e(TAG, "FCM 토큰 초기화 실패", e)
                }
        }
    }
}
