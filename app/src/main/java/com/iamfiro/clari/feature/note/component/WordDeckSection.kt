package com.iamfiro.clari.feature.note.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.service.KeywordHit
import com.iamfiro.clari.core.ui.theme.Dimens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

data class DetectedTerm(
    val id: String,
    val keyword: KeywordHit,
    val detectedAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordDeckSection(
    terms: List<DetectedTerm>,
    shouldTriggerHaptic: Boolean = false,
    onHapticTriggered: () -> Unit = {},
    onCardClick: (DetectedTerm) -> Unit = {},
    onCardLongPress: (DetectedTerm) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current

    val cardWidth = (configuration.screenWidthDp * 0.85f).dp

    var userHasScrolledAway by remember { mutableStateOf(false) }

    val firstVisibleItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { (isScrolling, _) -> isScrolling }
            .collect { (_, currentIndex) ->
                if (currentIndex > 0) {
                    userHasScrolledAway = true
                }
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { (isScrolling, _) -> !isScrolling }
            .collect { (_, currentIndex) ->
                if (currentIndex == 0) {
                    userHasScrolledAway = false
                }
            }
    }

    LaunchedEffect(terms.firstOrNull()?.id) {
        if (terms.isNotEmpty() && !userHasScrolledAway) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(shouldTriggerHaptic) {
        if (shouldTriggerHaptic) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onHapticTriggered()
        }
    }
    
    AnimatedVisibility(
        visible = terms.isNotEmpty(),
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            expandFrom = Alignment.Bottom
        ) + fadeIn(animationSpec = tween(200)),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            shrinkTowards = Alignment.Bottom
        ) + fadeOut(animationSpec = tween(150)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            LazyRow(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = terms,
                    key = { it.id }
                ) { term ->
                    DetectedTermCard(
                        term = term,
                        onClick = { onCardClick(term) },
                        onLongPress = { onCardLongPress(term) },
                        modifier = Modifier
                            .width(cardWidth)
                            .animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                    )
                }
            }
        }
    }
}


@Composable
private fun DetectedTermCard(
    term: DetectedTerm,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .padding(12.dp)
                .height(60.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    term.keyword.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 설명
                Text(
                    term.keyword.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
