package com.iamfiro.clari.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.LocalCurrentScreen
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen

@Composable
fun NavBar() {
    val currentScreen = LocalCurrentScreen.current;
    val backStack = LocalNavBackStack.current;

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 18.dp, bottom = 26.dp, start = 12.dp, end = 12.dp)
    ) {
        NavBarIcon(
            R.drawable.house, "home", currentScreen == Screen.Home,
            { backStack.add(Screen.Home) })
        NavBarIcon(
            R.drawable.file_audio,
            "audio",
            currentScreen == Screen.Note,
            { backStack.add(Screen.Note) })
        NavBarIcon(
            R.drawable.folder,
            "folder",
            currentScreen == Screen.ProjectList,
            { backStack.add(Screen.ProjectList) })
    }
}

@Composable
fun NavBarIcon(icon: Int, name: String, isActive: Boolean, onClick: () -> Boolean) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .size(26.dp)
            .alpha(if (isActive) 1f else 0.4f)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = name
        )
    }
}
