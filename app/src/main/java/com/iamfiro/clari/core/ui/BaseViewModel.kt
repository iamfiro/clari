package com.iamfiro.clari.core.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel : ViewModel() {
    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()
    
    protected fun navigateTo(screen: Screen) {
        _navigationEvent.trySend(NavigationEvent.NavigateTo(screen))
    }
    
    protected fun navigateBack() {
        _navigationEvent.trySend(NavigationEvent.NavigateBack)
    }
    
    protected fun navigateBackTo(screen: Screen) {
        _navigationEvent.trySend(NavigationEvent.NavigateBackTo(screen))
    }
    
    protected fun replaceCurrent(screen: Screen) {
        _navigationEvent.trySend(NavigationEvent.ReplaceCurrent(screen))
    }
}


