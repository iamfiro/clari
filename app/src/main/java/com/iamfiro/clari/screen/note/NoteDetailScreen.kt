package com.iamfiro.clari.screen.note

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.iamfiro.clari.core.Repository.NoteRepository
import com.iamfiro.clari.feature.note.component.AISummarySection
import com.iamfiro.clari.feature.note.component.NoteDetailControl
import com.iamfiro.clari.feature.note.component.TranscribeSection
import com.iamfiro.clari.feature.note.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NoteDetailScreen() {
    val repository = remember { NoteRepository() }
    var note by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            note = repository.getNoteById("note_001")
        }
    }

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
                        note?.name ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        note?.recordedAtText ?: "",
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
                item { AISummarySection(note?.aiSummary?.content ?: "") }
                item { TranscribeSection(note?.transcripts ?: emptyList()) }
            }

            NoteDetailControl()
        }
    }
}
