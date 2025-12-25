package com.iamfiro.clari.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.iamfiro.clari.screen.recording.BeforeRecordingScreen
import com.iamfiro.clari.screen.HomeScreen
import com.iamfiro.clari.screen.NoteDetailScreen
import com.iamfiro.clari.screen.NoteListScreen
import com.iamfiro.clari.screen.OnboardScreen
import com.iamfiro.clari.screen.Project.ProjectDetail
import com.iamfiro.clari.screen.Project.ProjectListScreen
import com.iamfiro.clari.screen.recording.RecordingScreen

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
            entry<Screen.NoteDetail> { NoteDetailScreen() }

            entry<Screen.ProjectList> { ProjectListScreen() }
            entry<Screen.ProjectDetail> { ProjectDetail() }

            // Recording
            entry<Screen.BeforeRecording> { BeforeRecordingScreen() }
            entry<Screen.Recording> { RecordingScreen() }
        }
    )
}
