package com.iamfiro.clari.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.user.Component.AppleLoginButton
import com.iamfiro.clari.feature.user.Component.GoogleLoginButton

@Composable
fun OnboardScreen() {
    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
        Box(Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color(0xFFA6C9EB)))
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
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
                    GoogleLoginButton()
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