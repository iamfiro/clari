package com.iamfiro.clari.feature.note.Component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.core.ui.theme.Dimens

@Composable
fun TranscribeContainer() {
    LazyColumn(
        modifier = Modifier
            .padding(Dimens.ScreenPadding, 12.dp)
    ) {
        item {
            Text(
                "아, 그런 상황이군요.\n안녕 아 나는 회의를 시작하겠습니다.\n자 이번 안건은 AWS 대규모 해킹사건에 대해 진상을 밝히려고 모두 모였어요",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                lineHeight = 56.sp
            )
        }
    }
}