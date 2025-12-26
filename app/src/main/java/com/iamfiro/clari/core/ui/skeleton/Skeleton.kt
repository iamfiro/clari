package com.iamfiro.clari.core.ui.skeleton

import androidx.compose.animation.core.*
import androidx.compose.runtime.*

@Composable
fun rememberSkeletonAlpha(
    min: Float = 0.05f,
    max: Float = 0.15f,
    durationMs: Int = 1000,
): Float {
    val transition = rememberInfiniteTransition(label = "skeleton_transition")
    val alpha by transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    return alpha
}