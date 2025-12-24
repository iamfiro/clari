package com.iamfiro.clari.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.AISummarySection
import com.iamfiro.clari.feature.note.component.NoteDetailControl
import com.iamfiro.clari.feature.note.component.TranscribeSection
import com.iamfiro.clari.feature.note.model.dummy_note_detail

@Composable
fun NoteDetailScreen() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Icon(painter = painterResource(R.drawable.arrow_left), "back")
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "발표 주제 '킥'과 관련된 아이디어 논의",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "2024년 4월 12일 12:11",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item { AISummarySection(dummy_note_detail.aiSummary.content) }
                item { TranscribeSection(dummy_note_detail.transcripts) }
            }

            NoteDetailControl()
        }
    }
}
