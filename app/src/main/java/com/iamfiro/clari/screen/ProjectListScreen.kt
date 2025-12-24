package com.iamfiro.clari.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.NewRecordingFloating
import com.iamfiro.clari.feature.project.component.ProjectCard
import com.iamfiro.clari.feature.project.model.dummy_project

@Composable
fun PackListScreen() {
    Scaffold(
        bottomBar = { NavBar() },
        floatingActionButton = { NewRecordingFloating() }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Header("프로젝트")

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 6
                    .dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        dummy_project.map { pack ->
                            ProjectCard(pack)
                        }
                    }
                }
            }
        }
    }
}
