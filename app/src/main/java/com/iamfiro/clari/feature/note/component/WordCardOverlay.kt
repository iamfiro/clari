package com.iamfiro.clari.feature.note.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.iamfiro.clari.R
import com.iamfiro.clari.core.service.KeywordHit
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.delay

@Composable
fun WordCardOverlay(
    words: List<Word>,
    maxVisible: Int = 3
) {
    val visibleWords = words.takeLast(maxVisible).reversed()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.ScreenPadding)
    ) {
        visibleWords.forEachIndexed { index, word ->
            val offsetY = index * 12.dp
            val scale = 1f - (index * 0.04f)
            val alpha = 1f - (index * 0.1f)

            Box(
                modifier = Modifier
                    .offset(y = offsetY)
                    .scale(scale)
                    .alpha(alpha)
                    .zIndex((maxVisible - index).toFloat())
            ) {
                WordCardOverlayItem(word)
            }
        }
    }
}

@Composable
fun WordCardOverlay(
    keywords: List<KeywordHit>,
    onDismiss: () -> Unit,
    maxVisible: Int = 3
) {
    val visibleKeywords = keywords.takeLast(maxVisible)
    var previousKeywords by remember { mutableStateOf<List<KeywordHit>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.ScreenPadding)
            .offset(y = 80.dp), // 키워드 카드를 아래로 내림
        contentAlignment = Alignment.TopCenter
    ) {
        visibleKeywords.reversed().forEachIndexed { index, keyword ->
            // 각 카드에 고유한 키 부여
            key(keyword.name + keyword.description) {
                AnimatedKeywordCard(
                    keyword = keyword,
                    index = index,
                    maxVisible = maxVisible,
                    isNewCard = !previousKeywords.contains(keyword)
                )
            }
        }
    }

    // 이전 키워드 목록 업데이트
    LaunchedEffect(keywords) {
        previousKeywords = keywords
    }
}

@Composable
private fun AnimatedKeywordCard(
    keyword: KeywordHit,
    index: Int,
    maxVisible: Int,
    isNewCard: Boolean
) {
    var isVisible by remember { mutableStateOf(false) }
    var hasAnimated by remember { mutableStateOf(false) }
    
    // 카드가 나타날 때 애니메이션 트리거
    LaunchedEffect(Unit) {
        if (isNewCard && !hasAnimated) {
            delay(50) // 약간의 딜레이로 애니메이션 시작
        }
        isVisible = true
        hasAnimated = true
    }
    
    // 스프링 기반 애니메이션
    val targetOffsetY = index * 12.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) targetOffsetY else targetOffsetY + 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offsetY"
    )
    
    val targetScale = 1f - (index * 0.04f)
    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) targetScale else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    val targetAlpha = 1f - (index * 0.1f)
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) targetAlpha else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "alpha"
    )
    
    // 회전 효과 (살짝 기울어지면서 등장)
    val animatedRotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .offset(y = animatedOffsetY)
            .zIndex((maxVisible - index).toFloat())
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
                rotationZ = animatedRotation
                // 3D 효과
                cameraDistance = 12f * density
            }
    ) {
        KeywordCardOverlayItem(keyword)
    }
}

@Composable
fun WordCardOverlayItem(word: Word) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.25f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.lightbulb),
                contentDescription = null,
                modifier = Modifier.size(16.dp).alpha(0.5f)
            )
            Text(
                "도메인 지식",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            word.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            word.meaning,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun KeywordCardOverlayItem(keyword: KeywordHit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.3f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.lightbulb),
                contentDescription = null,
                modifier = Modifier.size(16.dp).alpha(0.5f)
            )
            Text(
                "키워드 탐지",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            keyword.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            keyword.description,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
