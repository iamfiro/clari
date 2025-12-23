package com.iamfiro.clari.core.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Onboard : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Note : Screen

    @Serializable
    data object WordPack : Screen
}