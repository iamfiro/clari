package com.iamfiro.clari.feature.note.Component

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

// 문자 길이에 따른 폰트 사이즈 계산
private fun calculateFontSize(text: String): TextUnit {
    val length = text.length
    return when {
        length > 100 -> 20.sp   // 매우 긴 텍스트
        length > 70 -> 24.sp    // 긴 텍스트
        length > 50 -> 28.sp    // 중간 텍스트
        else -> 32.sp           // 기본 크기
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
    
    // 아이템 변경 시 (추가, formatted 업데이트) 자동 스크롤
    LaunchedEffect(transcriptItems) {
        val totalItems = transcriptItems.size + (if (partialText != null) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(index = totalItems - 1)
        }
    }
    
    // partial 변경 시에도 스크롤
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
        // 확정된 텍스트들 표시 (committed -> formatted로 자동 전환)
        items(
            items = transcriptItems,
            key = { it.id }
        ) { item ->
            TranscribeTextItem(item = item)
            Spacer(modifier = Modifier.height(14.dp))
        }
        
        // 실시간 인식 중인 텍스트 (Partial)
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
        
        // 텍스트가 없을 때 안내 메시지
        if (transcriptItems.isEmpty() && partialText == null) {
            item {
                Text(
                    "녹음을 시작하면 음성이 텍스트로 변환됩니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 확정된 텍스트 아이템 (committed/formatted)
 * formatted가 적용되면 애니메이션과 함께 텍스트가 변경됨
 */
@Composable
private fun TranscribeTextItem(
    item: TranscriptItem
) {
    // formatted 상태 변화 감지를 위한 플래그
    var showFormattedIndicator by remember { mutableStateOf(false) }
    var previousIsFormatted by remember { mutableStateOf(item.isFormatted) }
    
    // formatted가 새로 적용되면 인디케이터 표시
    LaunchedEffect(item.isFormatted) {
        if (item.isFormatted && !previousIsFormatted) {
            showFormattedIndicator = true
            delay(2000) // 2초 후 인디케이터 숨김
            showFormattedIndicator = false
        }
        previousIsFormatted = item.isFormatted
    }
    
    Column {
        // Formatted 인디케이터 (AI 교정됨 표시)
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
                    text = "✨ AI 교정됨",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // AnimatedContent로 텍스트 변경 시 부드러운 전환
        AnimatedContent(
            targetState = item.displayChunks to item.isFormatted,
            transitionSpec = {
                // formatted 전환 시 더 눈에 띄는 애니메이션
                if (targetState.second && !initialState.second) {
                    // committed -> formatted: 슬라이드 + 페이드
                    (fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 4 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 4 })
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            },
            label = "transcript_text_change"
        ) { (chunks, _) ->
            Column {
                chunks.forEachIndexed { index, chunk ->
                    // 각 청크별 폰트 사이즈 계산
                    val chunkFontSize = calculateFontSize(chunk)
                    val chunkLineHeight = calculateLineHeight(chunkFontSize)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chunk,
                            fontSize = chunkFontSize,
                            fontWeight = FontWeight.Medium,
                            lineHeight = chunkLineHeight,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * 실시간 인식 중인 텍스트 (Partial)
 */
@Composable
private fun PartialTextItem(
    chunks: List<String>
) {
    // 깜빡이는 효과를 위한 alpha 애니메이션
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
