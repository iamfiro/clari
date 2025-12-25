package com.iamfiro.clari.screen.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.Repository.NoteRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.AISummarySection
import com.iamfiro.clari.feature.note.component.TranscribeSection

@Composable
fun NoteDetailScreen(
    noteId: String,
) {
    val repository = remember { NoteRepository() }
    val viewModel = remember(noteId) { NoteDetailViewModel(repository, noteId) }

    val backStack = LocalNavBackStack.current
    
    val note by viewModel.note.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = "back",
                    modifier = Modifier.clickable { backStack.removeLastOrNull() }
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        note?.name ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        note?.recordedAtText ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "알 수 없는 오류가 발생했습니다.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        item { AISummarySection(note?.aiSummary?.content ?: "") }
                        item { TranscribeSection(note?.transcripts ?: emptyList()) }
                    }
                }
            }

            // 오디오 컨트롤
            NoteDetailControl(
                isPlaying = isPlaying,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSkipBackward = { viewModel.skipBackward() },
                onSkipForward = { viewModel.skipForward() }
            )
        }
    }
}

@Composable
private fun NoteDetailControl(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.back_skip_5sec),
            contentDescription = "back skip 5sec",
            modifier = Modifier
                .size(30.dp)
                .alpha(0.5f)
                .clickable { onSkipBackward() }
        )
        Icon(
            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
            contentDescription = if (isPlaying) "pause" else "play",
            modifier = Modifier
                .size(40.dp)
                .clickable { onPlayPauseClick() }
        )
        Icon(
            painter = painterResource(R.drawable.skip_5sec),
            contentDescription = "skip 5sec",
            modifier = Modifier
                .size(30.dp)
                .alpha(0.5f)
                .clickable { onSkipForward() }
        )
    }
}
