package com.iamfiro.clari.feature.project.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.feature.project.model.Project

@Composable
fun ProjectCard(project: Project?, frequentlyUsed: Boolean = false) {
    if (project == null) {
        ProjectCardSkeleton()
    } else {
        ProjectCardContent(project, frequentlyUsed)
    }
}

@Composable
private fun ProjectCardContent(project: Project, frequentlyUsed: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)

    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if(frequentlyUsed) {
                Text(
                    "자주 사용!",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Red
                    ,
                )
            }

            Text(
                project.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                "단어 ${project.word.size}개",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "By",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )

                Text(
                    project.publisherName,
                    style = MaterialTheme.typography.labelLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.sample_profile),
            contentDescription = project.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(74.dp)
                .height(74.dp)
                .clip(RoundedCornerShape(10.dp))
        )
    }
}

@Composable
private fun ProjectCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(102.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
            .padding(14.dp)
    ) {}
}
