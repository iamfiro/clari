package com.iamfiro.clari.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.Component.NewRecordingFloating
import com.iamfiro.clari.feature.note.Component.NoteCard
import com.iamfiro.clari.util.toRelativeDateLabel
import com.iamfiro.clwari.feature.note.model.dummy_notes
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen() {
    val today = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

    val groupedNotes = dummy_notes
        .groupBy { it.createdAt }
        .toSortedMap(compareByDescending { it })

    Scaffold(
        bottomBar = { NavBar() },
        floatingActionButton = { NewRecordingFloating(onClick = {}) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Header("노트")
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(
                    horizontal = Dimens.ScreenPadding,
                    vertical = 8.dp
                )
            ) {
                groupedNotes.forEach { (date, notes) ->
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Text(
                                toRelativeDateLabel(
                                    dateTime = date,
                                    now = today
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(1.dp))
                    }

                    items(
                        items = notes,
                    ) { note ->
                        NoteCard(note)
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                    }
                }

                item {
                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}