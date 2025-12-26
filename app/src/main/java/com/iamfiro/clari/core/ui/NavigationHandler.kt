package com.iamfiro.clari.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow

@Composable
fun HandleNavigationEvents(
    navigationEvent: Flow<NavigationEvent>,
    backStack: NavBackStack<NavKey>
) {
    LaunchedEffect(Unit) {
        navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    backStack.add(event.screen)
                }
                is NavigationEvent.NavigateBack -> {
                    backStack.removeLastOrNull()
                }
                is NavigationEvent.NavigateBackTo -> {
                    while (backStack.isNotEmpty() && backStack.lastOrNull() != event.screen) {
                        backStack.removeLastOrNull()
                    }
                }
                is NavigationEvent.ReplaceCurrent -> {
                    backStack.removeLastOrNull()
                    backStack.add(event.screen)
                }
            }
        }
    }
}

