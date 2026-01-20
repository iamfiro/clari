package com.iamfiro.clari.feature.note.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.theme.Dimens

@Composable
fun RecordingHeader(
    onExitClick: () -> Unit = {}, 
    onBackClick: () -> Unit = {},
    isPaused: Boolean = false
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_left),
                contentDescription = "back",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp), 
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    "Tap here to change title",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "December 24, 2025", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            TextButton(
                onClick = onExitClick,
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 12.dp)
            ) {
                Text(
                    "End",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "⏸ Paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
