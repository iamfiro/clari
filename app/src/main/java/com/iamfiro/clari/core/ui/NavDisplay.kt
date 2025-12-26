package com.iamfiro.clari.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.iamfiro.clari.screen.recording.project.RecordingProjectSelectionScreen
import com.iamfiro.clari.screen.home.HomeScreen
import com.iamfiro.clari.screen.note.detail.NoteDetailScreen
import com.iamfiro.clari.screen.note.list.NoteListScreen
import com.iamfiro.clari.screen.OnboardScreen
import com.iamfiro.clari.screen.project.create.ProjectCreateScreen
import com.iamfiro.clari.screen.project.detail.ProjectDetailScreen
import com.iamfiro.clari.screen.project.list.ProjectListScreen
import com.iamfiro.clari.screen.recording.language.LanguageSelectScreen
import com.iamfiro.clari.screen.recording.RecordingScreen
import com.iamfiro.clari.screen.externalresource.ExternalResourceListScreen
import com.iamfiro.clari.screen.externalresource.ExternalResourceDetailScreen

val LocalNavBackStack = staticCompositionLocalOf<NavBackStack<NavKey>> {
    error("NavBackStack not provided")
}

val LocalCurrentScreen = staticCompositionLocalOf<Screen> {
error("CurrentScreen not provided")
}

@Composable
fun AppNav() {
    val backStack = LocalNavBackStack.current

    NavDisplay(
        backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Screen.Onboard> { OnboardScreen() }

            entry<Screen.Home> { HomeScreen() }

            entry<Screen.Note> { NoteListScreen() }
            entry<Screen.NoteDetail> { NoteDetailScreen(noteId = it.noteId) }

            entry<Screen.ProjectList> { ProjectListScreen() }
            entry<Screen.ProjectCreate> { ProjectCreateScreen() }
            entry<Screen.ProjectDetail> { ProjectDetailScreen(projectId = it.projectId) }

            // Recording
            entry<Screen.BeforeRecording> { RecordingProjectSelectionScreen() }
            entry<Screen.LanguageSelectScreen> { LanguageSelectScreen(it.projectId) }
            entry<Screen.Recording> { 
                RecordingScreen(
                    projectId = it.projectId, 
                    languageCode = it.languageCode,
                    keywordPackIds = it.keywordPackIds,
                    externalResourceIds = it.externalResourceIds
                ) 
            }

            // External Resources
            entry<Screen.ExternalResourceList> { ExternalResourceListScreen() }
            entry<Screen.ExternalResourceDetail> { ExternalResourceDetailScreen(resourceId = it.resourceId) }
        }
    )
}
