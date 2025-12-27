package com.iamfiro.clari.feature.note.component.noteCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.skeleton.rememberSkeletonAlpha

@Composable
fun NoteCardSkeleton(size: Int = 3) {
    val alpha = rememberSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(size) {
            Skeleton(alpha)
        }
    }
}

@Composable
private fun Skeleton(alpha: Float = 1f) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(70.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
            .padding(14.dp)
    ) {}
}