package com.iamfiro.clari

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.ui.AppNav
import com.iamfiro.clari.core.ui.LocalCurrentScreen
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen

@Composable
fun AppRoot() {
    val tokenManager = remember {
        ApiClient.getTokenManager()
    }
    
    var initialScreen by remember { mutableStateOf<Screen?>(null) }

    LaunchedEffect(Unit) {
        val isLoggedIn = tokenManager.isLoggedIn()
        initialScreen = if (isLoggedIn) Screen.Home else Screen.Onboard
    }
    
    val backStack: NavBackStack<NavKey>? = if (initialScreen != null) {
        rememberNavBackStack(initialScreen!!)
    } else {
        null
    }

    val currentScreen = remember(backStack, initialScreen) {
        derivedStateOf {
            backStack?.lastOrNull() as? Screen ?: initialScreen ?: Screen.Onboard
        }
    }

    if (backStack != null && initialScreen != null) {
        CompositionLocalProvider(
            LocalNavBackStack provides backStack,
            LocalCurrentScreen provides currentScreen.value,
        ) {
            AppNav()
        }
    }
}
