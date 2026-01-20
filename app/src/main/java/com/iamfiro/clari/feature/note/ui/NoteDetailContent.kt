package com.iamfiro.clari.feature.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.AISummarySection
import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.feature.note.model.TranscriptWord
import com.iamfiro.clari.screen.note.detail.LinkedProject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteDetailContent(
    note: Note?,
    currentTranscriptIndex: Int,
    currentWordIndex: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    error: String?,
    onTranscriptClick: (TranscriptLine) -> Unit,
    onTranscriptLongPress: (TranscriptLine) -> Unit,
    onWordClick: (TranscriptWord) -> Unit,
    linkedProjects: List<LinkedProject> = emptyList(),
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

                if (linkedProjects.isNotEmpty()) {
                    item {
                        LinkedProjectsSection(
                            projects = linkedProjects,
                            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item {
                    Box(Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                        AISummarySection(note?.aiSummary?.content ?: "")
                    }
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    Box(Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                        Text(
                            "Transcript",
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
                        onLongPress = { onTranscriptLongPress(transcript) },
                        onWordClick = onWordClick
                    )
                }

                item { Spacer(Modifier.height(300.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkedProjectsSection(
    projects: List<LinkedProject>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(R.drawable.folder),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            projects.forEach { project ->
                ProjectChip(name = project.name)
            }
        }
    }
}

@Composable
private fun ProjectChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}
