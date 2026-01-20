package com.iamfiro.clari.feature.note.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.core.service.model.SttResponse
import com.iamfiro.clari.core.service.model.TranscriptItem
import com.iamfiro.clari.core.ui.theme.Dimens
import kotlinx.coroutines.delay

private fun calculateFontSize(text: String): TextUnit {
    val length = text.length
    return when {
        length > 100 -> 20.sp
        length > 70 -> 24.sp
        length > 50 -> 28.sp
        else -> 32.sp
    }
}

private fun calculateLineHeight(fontSize: TextUnit): TextUnit {
    return when {
        fontSize <= 20.sp -> 32.sp
        fontSize <= 24.sp -> 38.sp
        fontSize <= 28.sp -> 44.sp
        else -> 50.sp
    }
}

@Composable
fun TranscribeContainer(
    transcriptItems: List<TranscriptItem> = emptyList(),
    partialText: SttResponse? = null
) {
    val listState = rememberLazyListState()

    LaunchedEffect(transcriptItems) {
        val totalItems = transcriptItems.size + (if (partialText != null) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(index = totalItems - 1)
        }
    }

    LaunchedEffect(partialText) {
        val totalItems = transcriptItems.size + (if (partialText != null) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(index = totalItems - 1)
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(Dimens.ScreenPadding, 12.dp)
    ) {
        items(
            items = transcriptItems,
            key = { it.id }
        ) { item ->
            TranscribeTextItem(item = item)
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            AnimatedVisibility(
                visible = partialText != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                partialText?.let { response ->
                    PartialTextItem(chunks = response.chunks)
                }
            }
        }

        if (transcriptItems.isEmpty() && partialText == null) {
            item {
                Text(
                    "Voice will be transcribed to text when recording starts",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun TranscribeTextItem(
    item: TranscriptItem
) {
    var showFormattedIndicator by remember { mutableStateOf(false) }
    var previousIsFormatted by remember { mutableStateOf(item.isFormatted) }
    
    // Show indicator when formatted is newly applied
    LaunchedEffect(item.isFormatted) {
        if (item.isFormatted && !previousIsFormatted) {
            showFormattedIndicator = true
            delay(2000) // Hide indicator after 2 seconds
            showFormattedIndicator = false
        }
        previousIsFormatted = item.isFormatted
    }
    
    val textColor = if (item.isFormatted) {
        MaterialTheme.colorScheme.primary // Blue - Formatted
    } else {
        MaterialTheme.colorScheme.onSurface // Default - Committed
    }
    
    Column {
        // Formatted indicator (AI corrected badge)
        AnimatedVisibility(
            visible = showFormattedIndicator,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it },
            exit = fadeOut(tween(500))
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "✨ AI Corrected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        AnimatedContent(
            targetState = Triple(item.displayChunks, item.isFormatted, textColor),
            transitionSpec = {
                // More noticeable animation during formatted transition
                if (targetState.second && !initialState.second) {
                    // committed -> formatted: slide + fade
                    (fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 4 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 4 })
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            },
            label = "transcript_text_change"
        ) { (chunks, isFormatted, color) ->
            Column {
                chunks.forEachIndexed { index, chunk ->
                    // Calculate font size for each chunk
                    val chunkFontSize = calculateFontSize(chunk)
                    val chunkLineHeight = calculateLineHeight(chunkFontSize)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chunk,
                            fontSize = chunkFontSize,
                            fontWeight = if (isFormatted) FontWeight.Medium else FontWeight.Normal,
                            lineHeight = chunkLineHeight,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartialTextItem(
    chunks: List<String>
) {
    // Alpha animation for blinking effect
    var blinkState by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (blinkState) 0.7f else 0.5f,
        animationSpec = tween(500),
        label = "partial_blink"
    )
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(600)
            blinkState = !blinkState
        }
    }
    
    Column {
        chunks.forEach { chunk ->
            val fontSize = calculateFontSize(chunk)
            val lineHeight = calculateLineHeight(fontSize)
            
            Text(
                text = chunk,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                lineHeight = lineHeight,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        }
    }
}
