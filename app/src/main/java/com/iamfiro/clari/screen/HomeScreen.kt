package com.iamfiro.clari.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.component.Banner
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.NewRecordingFloating
import com.iamfiro.clari.feature.note.component.NoteCard
import com.iamfiro.clari.feature.project.component.WordCard
import com.iamfiro.clari.feature.project.model.dummy_words
import com.iamfiro.clari.feature.project.component.ProjectCard
import com.iamfiro.clari.feature.project.model.dummy_project
import com.iamfiro.clwari.feature.note.model.dummy_notes

@Composable
fun HomeScreen() {
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
                item { Banner() }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("최근 노트")

                        dummy_notes.take(3).map { note ->
                            NoteCard(note)
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("최근에 사용한 프로젝트")

                        dummy_project.take(2).map { pack ->
                            ProjectCard(pack)
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("자주 등장하는 단어")

                        dummy_words.chunked(2).forEach { row ->
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
