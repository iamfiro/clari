package com.iamfiro.clari.feature.note.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.feature.note.model.TranscriptWord
import com.iamfiro.clari.util.formatMmSs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranscriptItem(
    transcript: TranscriptLine,
    words: List<TranscriptWord>,
    allWords: List<TranscriptWord>,
    currentWordIndex: Int,
    isCurrentlyPlaying: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onWordClick: (TranscriptWord) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
        else
            Color.Transparent,
        animationSpec = tween(300),
        label = "bgColor"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = Dimens.ScreenPadding)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(vertical = 12.dp)
    ) {
        SpeakerAvatar(speaker = transcript.speaker,)

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
            if (words.isNotEmpty()) {
                val textColor = if (isHighlighted)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

                val currentWord = if (currentWordIndex >= 0 && currentWordIndex < allWords.size) {
                    allWords[currentWordIndex]
                } else null

                val annotatedString = buildAnnotatedString {
                    words.forEachIndexed { index, word ->
                        // 현재 재생 중인 단어인지 확인 (startMs와 endMs가 일치하는지 확인)
                        val isCurrentWord = currentWord != null &&
                                word.startMs == currentWord.startMs &&
                                word.endMs == currentWord.endMs

                        // 클릭 가능하도록 annotation 추가
                        pushStringAnnotation(
                            tag = "WORD",
                            annotation = index.toString()
                        )

                        withStyle(
                            style = SpanStyle(
                                fontWeight = if (isCurrentWord && (isCurrentlyPlaying || isHighlighted)) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                fontSize = 15.sp,
                                background = if (isCurrentWord && (isCurrentlyPlaying || isHighlighted))
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else
                                    Color.Transparent
                            )
                        ) {
                            append(word.text)
                        }

                        pop()
                    }
                }

                ClickableText(
                    text = annotatedString,
                    style = TextStyle(
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    ),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "WORD",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let { annotation ->
                            val wordIndex = annotation.item.toIntOrNull()
                            if (wordIndex != null && wordIndex in words.indices) {
                                onWordClick(words[wordIndex])
                            }
                        }
                    }
                )
            } else {
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
}

