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
    data class NoteDetail(val noteId: String) : Screen

    @Serializable
    data object ProjectList : Screen

    @Serializable
    data class ProjectDetail(val projectId: String) : Screen

    // Recording
    @Serializable
    data class LanguageSelectScreen(val projectId: String) : Screen

    @Serializable
    data object BeforeRecording : Screen

    @Serializable
    data class Recording(val projectId: String, val languageCode: String) : Screen
}
