package com.iamfiro.clari.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.NewRecordingFloating
import com.iamfiro.clari.feature.note.component.noteCard.NoteCard
import com.iamfiro.clari.feature.note.component.noteCard.NoteCardSkeleton
import com.iamfiro.clari.feature.project.component.WordCard
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCard
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCardSkeleton

@Composable
fun HomeScreen() {
    val backStack = LocalNavBackStack.current
    val viewModel: HomeViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { NavBar() },
        floatingActionButton = { NewRecordingFloating() }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Header()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.isLoading) {
                            NoteCardSkeleton()
                        } else {
                            uiState.notes.take(3).forEach { note ->
                                NoteCard(note, onClick = { backStack.add(Screen.NoteDetail(note.id)) })
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("최근에 사용한 프로젝트")

                        if (uiState.isLoading) {
                            ProjectCardSkeleton()
                        } else {
                            uiState.keywordPacks.take(2).forEach { pack ->
                                ProjectCard(pack, onClick = { backStack.add(Screen.ProjectDetail(pack.id)) })
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("자주 등장하는 단어")

                        uiState.words.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WordCard(
                                    word = row[0],
                                    modifier = Modifier.weight(1f)
                                )

                                if (row.size == 2) {
                                    WordCard(
                                        word = row[1],
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
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
