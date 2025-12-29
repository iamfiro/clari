package com.iamfiro.clari.screen.note.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.iamfiro.clari.feature.note.component.WordDeckSection
import com.iamfiro.clari.feature.note.ui.AudioControlSection
import com.iamfiro.clari.feature.note.ui.NoteDetailContent
import com.iamfiro.clari.feature.note.ui.NoteDetailHeader

@Composable
fun NoteDetailScreen(
    noteId: String,
) {
    val viewModel = remember(noteId) { NoteDetailViewModel(noteId) }

    val uiState by viewModel.uiState.collectAsState()

    var headerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NoteDetailContent(
                note = uiState.note,
                currentTranscriptIndex = uiState.currentTranscriptIndex,
                currentWordIndex = uiState.currentWordIndex,
                isPlaying = uiState.isPlaying,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onTranscriptClick = { transcript -> 
                    viewModel.onTranscriptClicked(transcript)
                },
                onWordClick = { word ->
                    viewModel.onWordClicked(word)
                },
                linkedProjects = uiState.linkedProjects,
                headerHeight = headerHeight
            )
        }

        Box(
            modifier = Modifier
                .zIndex(1f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) {
                        coordinates.size.height.toDp()
                    }
                }
        ) {
            NoteDetailHeader(
                noteName = uiState.note?.name.orEmpty(),
                recordedAtText = uiState.note?.recordedAtText ?: ""
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp)
        ) {
            WordDeckSection(
                terms = uiState.displayedTerms,
                shouldTriggerHaptic = uiState.shouldTriggerHaptic,
                onHapticTriggered = { viewModel.onHapticTriggered() },
                onCardClick = { term ->
                    val targetWord = uiState.note?.words?.firstOrNull {
                        it.text.equals(term.keyword.name, ignoreCase = true)
                    }
                    targetWord?.let {
                        viewModel.seekTo(it.startMs)
                        if (!uiState.isPlaying && uiState.isMediaReady) {
                            viewModel.togglePlayPause()
                        }
                    }
                }
            )

            AudioControlSection(
                isPlaying = uiState.isPlaying,
                isMediaReady = uiState.isMediaReady,
                isBuffering = uiState.isBuffering,
                currentPositionMs = uiState.currentPositionMs,
                totalDurationMs = uiState.totalDurationMs,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSkipBackward = { viewModel.skipBackward() },
                onSkipForward = { viewModel.skipForward() },
                onSeek = { viewModel.seekTo(it) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
