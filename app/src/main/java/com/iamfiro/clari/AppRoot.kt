package com.iamfiro.clari

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.iamfiro.clari.core.ui.AppNav
import com.iamfiro.clari.core.ui.LocalCurrentScreen
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen

@Composable
fun AppRoot() {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(Screen.Home)

    val currentScreen = remember {
        derivedStateOf {
            backStack.lastOrNull() as? Screen ?: Screen.Home
        }
    }

    CompositionLocalProvider(
        LocalNavBackStack provides backStack,
        LocalCurrentScreen provides currentScreen.value,
    ) {
        AppNav()
    }
}
