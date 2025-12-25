package com.iamfiro.clari.feature.project.component

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
fun ProjectCard(project: Project, frequentlyUsed: Boolean = false) {
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
