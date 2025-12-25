package com.iamfiro.clari.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.theme.Dimens
import kotlin.math.roundToInt

data class BottomSheetConfig(
    val showDragHandle: Boolean = true,
    val dismissOnBackdropClick: Boolean = true,
    val showBackdrop: Boolean = true,
    val backdropAlpha: Float = 0.5f,
    val cornerRadius: Int = 28,
    val enableSwipeToDismiss: Boolean = true,
    val swipeDismissThreshold: Float = 100f
)

sealed class BottomSheetAction {
    data class Primary(
        val text: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true
    ) : BottomSheetAction()

    data class Secondary(
        val text: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true
    ) : BottomSheetAction()

    data class Destructive(
        val text: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true
    ) : BottomSheetAction()

    data class Text(
        val text: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true
    ) : BottomSheetAction()
}

@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    config: BottomSheetConfig = BottomSheetConfig(),
    content: @Composable ColumnScope.() -> Unit
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (visible) dragOffset else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "offset_animation"
    )

    if (visible) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (config.showBackdrop) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = config.backdropAlpha))
                            .then(
                                if (config.dismissOnBackdropClick) {
                                    Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { onDismiss() }
                                } else Modifier
                            )
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                )
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, animatedOffset.roundToInt()) }
                        .clip(
                            RoundedCornerShape(
                                topStart = config.cornerRadius.dp,
                                topEnd = config.cornerRadius.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .then(
                            if (config.enableSwipeToDismiss) {
                                Modifier.pointerInput(Unit) {
                                    detectVerticalDragGestures(
                                        onDragEnd = {
                                            if (dragOffset > config.swipeDismissThreshold) {
                                                onDismiss()
                                            }
                                            dragOffset = 0f
                                        },
                                        onVerticalDrag = { _, dragAmount ->
                                            dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                                        }
                                    )
                                }
                            } else Modifier
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* 클릭 이벤트 전파 방지 */ }
                ) {
                    Spacer(Modifier.height(26.dp))

                    content()
                }
            }
        }
    }
}

@Composable
fun BottomSheetWithHeader(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    config: BottomSheetConfig = BottomSheetConfig(),
    actions: List<BottomSheetAction> = emptyList(),
    content: @Composable ColumnScope.() -> Unit
) {
    BottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
        config = config
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenPadding)
        ) {
            content()
        }

        if (actions.isNotEmpty()) {
            BottomSheetActions(actions = actions)
        }
    }
}

@Composable
fun ConfirmBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "확인",
    cancelText: String = "취소",
    onConfirm: () -> Unit,
    onCancel: () -> Unit = onDismiss,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    config: BottomSheetConfig = BottomSheetConfig()
) {
    BottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
        config = config
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(cancelText, style = MaterialTheme.typography.bodyLarge)
                }

                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(9999.dp),
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(confirmText, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun <T> ListBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    itemContent: @Composable (T, Boolean) -> Unit,
    config: BottomSheetConfig = BottomSheetConfig()
) {
    BottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
        config = config
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onItemSelected(item)
                            onDismiss()
                        }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .padding(horizontal = Dimens.ScreenPadding, vertical = 14.dp)
                ) {
                    itemContent(item, isSelected)
                }
            }
        }
    }
}

data class BottomSheetMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: Painter? = null,
    val isDestructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

@Composable
fun MenuBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    items: List<BottomSheetMenuItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
    config: BottomSheetConfig = BottomSheetConfig()
) {
    BottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
        config = config
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = Dimens.ScreenPadding)
                    .padding(bottom = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            items.forEach { item ->
                MenuItemRow(
                    item = item,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: BottomSheetMenuItem,
    onDismiss: () -> Unit
) {
    val contentColor = when {
        !item.enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        item.isDestructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.enabled) {
                item.onClick()
                onDismiss()
            }
            .padding(horizontal = Dimens.ScreenPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (item.icon != null) {
            Icon(
                painter = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.enabled) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomSheetActions(
    actions: List<BottomSheetAction>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            when (action) {
                is BottomSheetAction.Primary -> {
                    Button(
                        onClick = action.onClick,
                        enabled = action.enabled,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(action.text)
                    }
                }

                is BottomSheetAction.Secondary -> {
                    Button(
                        onClick = action.onClick,
                        enabled = action.enabled,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(action.text)
                    }
                }

                is BottomSheetAction.Destructive -> {
                    Button(
                        onClick = action.onClick,
                        enabled = action.enabled,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(action.text)
                    }
                }

                is BottomSheetAction.Text -> {
                    TextButton(
                        onClick = action.onClick,
                        enabled = action.enabled,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(action.text)
                    }
                }
            }
        }
    }
}
