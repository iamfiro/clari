package com.iamfiro.clari.screen.note

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.repository.NoteRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.AISummarySection
import com.iamfiro.clari.feature.note.model.Speaker
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.util.formatMmSs

@Composable
fun NoteDetailScreen(
    noteId: String,
) {
    val repository = remember { NoteRepository.getInstance() }
    val viewModel = remember(noteId) { NoteDetailViewModel(repository, noteId) }

    val backStack = LocalNavBackStack.current
    
    val note by viewModel.note.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val totalDurationMs by viewModel.totalDurationMs.collectAsState()
    val isMediaReady by viewModel.isMediaReady.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val currentTranscriptIndex by viewModel.currentTranscriptIndex.collectAsState()
    
    val listState = rememberLazyListState()
    
    // 현재 재생 중인 transcript로 자동 스크롤
    LaunchedEffect(currentTranscriptIndex) {
        if (currentTranscriptIndex >= 0 && isPlaying) {
            listState.animateScrollToItem(
                // AI 요약 섹션이 있으므로 +1
                index = currentTranscriptIndex + 1,
                scrollOffset = -100
            )
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 헤더
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding)
                    .padding(vertical = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = "back",
                    modifier = Modifier.clickable { backStack.removeLastOrNull() }
                )
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = note?.name.orEmpty(),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        note?.recordedAtText ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

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
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ScreenPadding)
                    ) {
                        // AI 요약 섹션
                        item { 
                            AISummarySection(note?.aiSummary?.content ?: "") 
                        }
                        
                        // 음성 기록 헤더
                        item {
                            Text(
                                "음성 기록", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val transcripts = note?.transcripts.orEmpty()

                        items(
                            items = transcripts,
                            key = { it.timeSec } // 가능하면 꼭 추가
                        ) { transcript ->
                            val index = transcripts.indexOf(transcript)

                            TranscriptItem(
                                transcript = transcript,
                                isCurrentlyPlaying = index == currentTranscriptIndex && isPlaying,
                                isHighlighted = index == currentTranscriptIndex,
                                onClick = { viewModel.seekToTranscript(transcript) }
                            )
                        }


                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }

            // 오디오 컨트롤
            AudioControlSection(
                isPlaying = isPlaying,
                isMediaReady = isMediaReady,
                isBuffering = isBuffering,
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSkipBackward = { viewModel.skipBackward() },
                onSkipForward = { viewModel.skipForward() },
                onSeek = { viewModel.seekTo(it) }
            )
        }
    }
}

@Composable
private fun TranscriptItem(
    transcript: TranscriptLine,
    isCurrentlyPlaying: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else 
            Color.Transparent,
        animationSpec = tween(300),
        label = "bgColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isCurrentlyPlaying) 1.02f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        // 스피커 아바타
        SpeakerAvatar(
            speaker = transcript.speaker,
            isActive = isCurrentlyPlaying
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    transcript.speaker.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    formatMmSs(transcript.timeSec),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                transcript.text,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = if (isHighlighted)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SpeakerAvatar(
    speaker: Speaker,
    isActive: Boolean
) {
    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFF64B5F6), // Blue
        Color(0xFF81C784), // Green
        Color(0xFFFFB74D),
        Color(0xFFBA68C8),
        Color(0xFF4DD0E1),
    )
    
    // speaker.id를 기반으로 색상 선택 (숫자 추출)
    val colorIndex = speaker.id.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toInt() ?: 0
    val baseColor = colors[colorIndex % colors.size]
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = tween(200),
        label = "avatarScale"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
                )
            )
    ) {
        Text(
            text = (colorIndex + 1).toString(),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AudioControlSection(
    isPlaying: Boolean,
    isMediaReady: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    onPlayPauseClick: () -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(horizontal = Dimens.ScreenPadding)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        // 타임라인 슬라이더
        Column {
            Slider(
                value = if (totalDurationMs > 0) currentPositionMs.toFloat() / totalDurationMs.toFloat() else 0f,
                onValueChange = { fraction ->
                    val newPosition = (fraction * totalDurationMs).toLong()
                    onSeek(newPosition)
                },
                enabled = isMediaReady,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // 시간 표시
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    formatMmSs((currentPositionMs / 1000).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    formatMmSs((totalDurationMs / 1000).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // 컨트롤 버튼
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 5초 뒤로
            Icon(
                painter = painterResource(R.drawable.back_skip_5sec),
                contentDescription = "5초 뒤로",
                modifier = Modifier
                    .size(36.dp)
                    .alpha(if (isMediaReady) 0.7f else 0.3f)
                    .clickable(enabled = isMediaReady) { onSkipBackward() }
            )
            
            Spacer(Modifier.width(32.dp))
            
            // 재생/일시정지
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(enabled = isMediaReady && !isBuffering) { onPlayPauseClick() }
            ) {
                when {
                    isBuffering -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    !isMediaReady -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else -> {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = if (isPlaying) "일시정지" else "재생",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(32.dp))
            
            // 5초 앞으로
            Icon(
                painter = painterResource(R.drawable.skip_5sec),
                contentDescription = "5초 앞으로",
                modifier = Modifier
                    .size(36.dp)
                    .alpha(if (isMediaReady) 0.7f else 0.3f)
                    .clickable(enabled = isMediaReady) { onSkipForward() }
            )
        }
    }
}
