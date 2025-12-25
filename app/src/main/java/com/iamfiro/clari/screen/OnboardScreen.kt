package com.iamfiro.clari.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.iamfiro.clari.feature.user.component.AppleLoginButton
import com.iamfiro.clari.feature.user.component.GoogleLoginButton

private const val TAG = "OnboardScreen"

@Composable
fun OnboardScreen() {
    val context = LocalContext.current
    
    // Google Sign-In Options 설정 (idToken 요청)
    val webClientId = context.getString(com.iamfiro.clari.R.string.default_web_client_id)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }
    
    // Google Sign-In 결과 처리
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            
            // idToken 가져오기 및 로그 출력
            val idToken = account?.idToken
            if (idToken != null) {
                Log.d(TAG, "========== idToken ==========")
                Log.d(TAG, idToken)
                Log.d(TAG, "============================")
            } else {
                Log.e(TAG, "idToken이 null입니다.")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In 실패 (코드: ${e.statusCode}): ${e.message}")
            if (e.statusCode == 10) {
                Log.e(TAG, "에러 코드 10: Google Cloud Console에서 Android OAuth 클라이언트에 SHA-1 지문을 등록해주세요.")
                Log.e(TAG, "SHA-1: FF:16:35:9D:0B:50:AF:9A:75:5C:BD:05:71:85:55:DD:48:41:3B:D9")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In 오류", e)
        }
    }
    
    val startGoogleSignIn: () -> Unit = {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
    
    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFA6C9EB))
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(32.dp)
                .weight(1f)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        append("어느 상황에서도\n회의 중 모르는 내용은\n")
                    }

                    append("Clari가 쉽게 정리해드려요")
                },
                style = MaterialTheme.typography.headlineMedium,
                lineHeight = 38.sp
            )
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoogleLoginButton(onClick = startGoogleSignIn)
                    AppleLoginButton()
                }
                Text(
                    "계속 진행하시면 이용약관 및 개인정보처리방침에 동의합니다.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
