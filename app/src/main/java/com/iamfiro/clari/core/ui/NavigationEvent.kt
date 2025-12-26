package com.iamfiro.clari.core.ui

sealed interface NavigationEvent {
    data class NavigateTo(val screen: Screen) : NavigationEvent
    data object NavigateBack : NavigationEvent
    data class NavigateBackTo(val screen: Screen) : NavigationEvent
    data class ReplaceCurrent(val screen: Screen) : NavigationEvent
}

