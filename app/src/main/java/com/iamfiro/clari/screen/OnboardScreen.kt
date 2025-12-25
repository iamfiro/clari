package com.iamfiro.clari.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.iamfiro.clari.R
import com.iamfiro.clari.core.database.AppDatabase
import com.iamfiro.clari.core.database.entity.TokenEntity
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.feature.user.component.AppleLoginButton
import com.iamfiro.clari.feature.user.component.GoogleLoginButton
import kotlinx.coroutines.launch

@Composable
fun OnboardScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current

    BackHandler(enabled = true) {
    }

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    val database = remember {
        AppDatabase.getInstance(context)
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
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = getCredentialRequest,
                    context = context
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val idToken = googleIdTokenCredential.idToken

                database.tokenDao().saveToken(TokenEntity(idToken = idToken))

                backStack.clear()
                backStack.add(Screen.Home)

            } catch (e: GetCredentialException) {

            } catch (e: GoogleIdTokenParsingException) {

            } catch (e: Exception) {

            }
        }
    }
    
    val isDarkMode = isSystemInDarkTheme()
    val topBoxColor = if (isDarkMode) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        androidx.compose.ui.graphics.Color(0xFFA6C9EB)
    }
    
    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(topBoxColor)
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
                    GoogleLoginButton(onClick = { handleGoogleSignIn() })
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
