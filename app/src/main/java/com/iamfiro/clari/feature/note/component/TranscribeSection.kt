package com.iamfiro.clari.feature.note.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamfiro.clari.feature.note.model.TranscriptLine
import com.iamfiro.clari.util.formatMmSs

@Composable
fun TranscribeSection(transcribe: List<TranscriptLine>) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Transcript", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        transcribe.map { transcribe ->
            Transcribe(transcribe)
        }
    }
}

@Composable
private fun Transcribe(transcribe: TranscriptLine) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier
            .width(35.dp)
            .height(35.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            Text(
                transcribe.speaker.id.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.let { "${it.toInt() + 1}" } ?: "1",
                fontWeight = FontWeight.Bold
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    transcribe.speaker.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatMmSs(transcribe.timeSec),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(transcribe.text, fontSize = 16.sp, lineHeight = 22.sp)
        }
    }
}
