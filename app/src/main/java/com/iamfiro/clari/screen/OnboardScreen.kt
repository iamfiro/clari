package com.iamfiro.clari.screen

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.iamfiro.clari.R
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.repository.AuthRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.user.component.AppleLoginButton
import com.iamfiro.clari.feature.user.component.GoogleLoginButton
import kotlinx.coroutines.launch

private const val TAG = "OnboardScreen"

@Composable
fun OnboardScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current

    var isLoading by remember { mutableStateOf(false) }

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    val authRepository = remember {
        AuthRepository.getInstance(ApiClient.getTokenManager())
    }

    val webClientId = stringResource(R.string.default_web_client_id)

    val googleIdOption: GetGoogleIdOption = remember(webClientId) {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
    }

    val getCredentialRequest = remember(googleIdOption) {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun handleGoogleSignIn() {
        Log.d(TAG, "handleGoogleSignIn() 호출됨")
        if (isLoading) {
            Log.d(TAG, "이미 로딩 중이므로 반환")
            return
        }

        coroutineScope.launch {
            Log.d(TAG, "구글 로그인 프로세스 시작")
            isLoading = true
            try {
                // 1. Google Credential Manager로 idToken 획득
                Log.d(TAG, "Credential 요청 시작 - webClientId: $webClientId")
                val result = credentialManager.getCredential(
                    request = getCredentialRequest,
                    context = context
                )
                Log.d(TAG, "Credential 획득 성공")

                val credential = result.credential
                Log.d(
                    TAG,
                    "Credential 타입: ${credential.type}, 데이터 크기: ${credential.data.size()} bytes"
                )

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                Log.d(TAG, "GoogleIdTokenCredential 생성 완료")

                val idToken = googleIdTokenCredential.idToken
                val idTokenPreview = if (idToken.length > 50) {
                    "${idToken.take(30)}...${idToken.takeLast(20)}"
                } else {
                    idToken
                }
                Log.d(TAG, "Google idToken 획득 완료 - 길이: ${idToken.length}, 미리보기: $idTokenPreview")
                Log.d(TAG, "Google 계정 이메일: ${googleIdTokenCredential.id}")

                // 2. 백엔드 API로 인증
                Log.d(TAG, "백엔드 API 인증 요청 시작")
                val authResult = authRepository.loginWithGoogle(idToken)
                Log.d(TAG, "백엔드 API 인증 요청 완료")

                authResult.fold(
                    onSuccess = { response ->
                        Log.d(
                            TAG,
                            "백엔드 인증 성공 - 이메일: ${response.user.email}, 사용자 ID: ${response.user.id}"
                        )
                        Log.d(TAG, "홈 화면으로 이동")
                        backStack.clear()
                        backStack.add(Screen.Home)
                    },
                    onFailure = { error ->
                        Log.e(
                            TAG,
                            "백엔드 인증 실패 - 에러 타입: ${error.javaClass.simpleName}, 메시지: ${error.message}",
                            error
                        )
                        error.stackTrace.take(5).forEach {
                            Log.e(
                                TAG,
                                "  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
                            )
                        }
                        Toast.makeText(
                            context,
                            "로그인에 실패했습니다: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential 획득 실패 - 타입: ${e.javaClass.simpleName}, 메시지: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(context, "Google 로그인이 취소되었습니다", Toast.LENGTH_SHORT).show()
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Google 토큰 파싱 실패 - 메시지: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(context, "인증 정보 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "로그인 중 예상치 못한 오류 발생 - 타입: ${e.javaClass.simpleName}, 메시지: ${e.message}",
                    e
                )
                e.printStackTrace()
                Toast.makeText(context, "로그인 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                Log.d(TAG, "구글 로그인 프로세스 종료 - isLoading: false")
            }
        }
    }

    val isDarkMode = isSystemInDarkTheme()
    val topBoxColor = if (isDarkMode) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        androidx.compose.ui.graphics.Color(0xFFA6C9EB)
    }

    Box(modifier = Modifier.fillMaxHeight()) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(topBoxColor)
                    .padding(horizontal = Dimens.ScreenPadding),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.service),
                    contentDescription = "service",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
            }
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

                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("Clari가 쉽게 정리해드려요")
                        }

                    },
                    style = MaterialTheme.typography.headlineMedium,
                    lineHeight = 38.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GoogleLoginButton(
                            onClick = { handleGoogleSignIn() },
                            enabled = !isLoading
                        )
                        AppleLoginButton(enabled = !isLoading)
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

        // 로딩 인디케이터
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
