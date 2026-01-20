package com.iamfiro.clari.feature.note.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen

@Composable
fun NewRecordingFloating(
    modifier: Modifier = Modifier,
) {
    val backStack = LocalNavBackStack.current

    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = { backStack.add(Screen.BeforeRecording) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.mic),
                contentDescription = "Add recording",
                Modifier.size(20.dp)
            )
        },
        text = {
            Text(
                text = "Record",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}
