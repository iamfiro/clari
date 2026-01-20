package com.iamfiro.clari.screen.note.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.theme.Dimens
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.feature.note.component.NewRecordingFloating
import com.iamfiro.clari.feature.note.component.noteCard.NoteCard
import com.iamfiro.clari.feature.note.component.noteCard.NoteCardSkeleton
import com.iamfiro.clari.util.getTodayDateTime
import com.iamfiro.clari.util.toRelativeDateLabel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen() {
    val backStack = LocalNavBackStack.current
    val viewModel: NoteListViewModel = viewModel()

    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val today = getTodayDateTime()

    val groupedNotes = notes
        .groupBy { it.createdAt.toLocalDate() }
        .toSortedMap(compareByDescending { it })

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        bottomBar = { NavBar() },
        floatingActionButton = { NewRecordingFloating() }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(Modifier.fillMaxSize()) {
                Header("Notes")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
                ) {
                if (isLoading) {
                    item { NoteCardSkeleton() }
                } else {
                    groupedNotes.forEach { (date, notesForDate) ->
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                Text(
                                    toRelativeDateLabel(
                                        dateTime = date.atStartOfDay(),
                                        now = today
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        items(
                            items = notesForDate,
                        ) { note ->
                            NoteCard(note, onClick = { backStack.add(Screen.NoteDetail(note.id)) })
                            Spacer(Modifier.height(4.dp))
                        }

                        item {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(50.dp))
                }
                }
            }
        }
    }
}
