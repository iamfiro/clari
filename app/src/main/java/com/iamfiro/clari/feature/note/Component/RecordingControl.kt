package com.iamfiro.clari.feature.note.Component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.service.ConnectionState
import com.iamfiro.clari.core.ui.theme.Dimens

private const val TAG = "RecordingControl"

@Composable
fun RecordingControl(
    isRecording: Boolean = false,
    elapsedTime: String = "00:00",
    connectionState: ConnectionState = ConnectionState.Disconnected,
    onToggleRecording: () -> Unit = {}
) {
    Log.d(TAG, "RecordingControl 렌더링 - isRecording: $isRecording, connectionState: $connectionState")
    
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                "소요시간",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                elapsedTime,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            // 연결 상태 표시
            ConnectionStatusIndicator(connectionState)
        }

        // 녹음 버튼
        Box(
            modifier = Modifier
                .height(65.dp)
                .width(65.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (isRecording) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                )
                .border(
                    width = 2.dp,
                    color = if (isRecording) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = RoundedCornerShape(999.dp)
                )
                .clickable {
                    Log.d(TAG, "========== 버튼 클릭됨! ==========")
                    onToggleRecording()
                },
            contentAlignment = Alignment.Center
        ) {
            when (connectionState) {
                is ConnectionState.Connecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                else -> {
                    Icon(
                        painter = painterResource(
                            if (isRecording) R.drawable.pause else R.drawable.pause
                        ),
                        contentDescription = if (isRecording) "녹음 중지" else "녹음 시작",
                        tint = if (isRecording) {
                            MaterialTheme.colorScheme.error
                        } else if (isSystemInDarkTheme()) {
                            Color(0xFFFFFFFF)
                        } else {
                            Color(0xFF000000)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(connectionState: ConnectionState) {
    val (text, color) = when (connectionState) {
        is ConnectionState.Disconnected -> "연결 대기" to MaterialTheme.colorScheme.onSurfaceVariant
        is ConnectionState.Connecting -> "연결 중..." to MaterialTheme.colorScheme.primary
        is ConnectionState.Connected -> "연결됨" to Color(0xFF4CAF50)
        is ConnectionState.Error -> "연결 오류" to MaterialTheme.colorScheme.error
    }
    
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}
