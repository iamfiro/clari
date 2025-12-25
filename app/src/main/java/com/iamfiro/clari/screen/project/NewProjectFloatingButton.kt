package com.iamfiro.clari.screen.project

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
fun NewProjectFloatingButton(
    modifier: Modifier = Modifier,
) {
    val backStack = LocalNavBackStack.current;

    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = { backStack.add(Screen.BeforeRecording) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.folder_plus),
                contentDescription = "프로젝트 추가",
                Modifier.size(20.dp)
            )
        },
        text = {
            Text(
                text = "프로젝트 추가",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}
