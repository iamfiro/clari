package com.iamfiro.clari.feature.note.component

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.model.Word

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
