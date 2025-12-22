package com.iamfiro.clari.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R

@Composable
fun NavBar() {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 18.dp, bottom = 26.dp, start = 12.dp, end = 12.dp)
    ) {
        NavBarIcon(R.drawable.house, "home", false)
        NavBarIcon(R.drawable.file_audio, "audio", false)
        NavBarIcon(R.drawable.folder, "folder", false)
        NavBarIcon(R.drawable.user, "user", false)
    }
}

@Composable
fun NavBarIcon(icon: Int, name: String, isActive: Boolean) {
    Icon(
        painter = painterResource(icon),
        contentDescription = name,
        Modifier
            .size(26.dp)
            .alpha(if (isActive) 1f else 0.4f)
    )
}

