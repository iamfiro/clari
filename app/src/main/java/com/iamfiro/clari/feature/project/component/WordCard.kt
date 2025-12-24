package com.iamfiro.clari.feature.project.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.feature.project.model.Word

@Composable
fun WordCard(word: Word, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
            .height(65.dp)
    ) {
        Text(word.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(
            word.meaning,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}
