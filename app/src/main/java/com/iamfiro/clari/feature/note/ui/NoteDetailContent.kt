package com.iamfiro.clari.feature.note.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.AISummarySection
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.feature.note.model.TranscriptWord

@Composable
fun NoteDetailContent(
    note: Note?,
    currentTranscriptIndex: Int,
    currentWordIndex: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    error: String?,
    onTranscriptClick: (TranscriptLine) -> Unit,
    onWordClick: (TranscriptWord) -> Unit,
    headerHeight: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentTranscriptIndex) {
        if (currentTranscriptIndex >= 0 && isPlaying) {
            listState.animateScrollToItem(
                index = currentTranscriptIndex + 1,
                scrollOffset = -100
            )
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(
                modifier = modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxWidth()
            ) {
                item { Spacer(Modifier.height(headerHeight + 32.dp)) }

                item {
                    Box(Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                        AISummarySection(note?.aiSummary?.content ?: "")
                    }
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    Box(Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                        Text(
                            "음성 기록",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                val transcripts = note?.transcripts.orEmpty()

                items(
                    items = transcripts,
                    key = { "${it.speaker} ${it.text} ${it.startMs}" }
                ) { transcript ->
                    val index = transcripts.indexOf(transcript)

                    val transcriptWords = note?.words?.filter { word ->
                        word.startMs >= transcript.startMs && word.startMs < transcript.endMs
                    } ?: emptyList()

                    val allWords = note?.words ?: emptyList()

                    TranscriptItem(
                        transcript = transcript,
                        words = transcriptWords,
                        allWords = allWords,
                        currentWordIndex = currentWordIndex,
                        isCurrentlyPlaying = index == currentTranscriptIndex && isPlaying,
                        isHighlighted = index == currentTranscriptIndex,
                        onClick = { onTranscriptClick(transcript) },
                        onWordClick = onWordClick
                    )
                }

                item { Spacer(Modifier.height(210.dp)) }
            }
        }
    }
}

