package com.iamfiro.clari.screen.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.note.component.NewRecordingFloating
import com.iamfiro.clari.feature.project.component.ProjectCard

@Composable
fun ProjectListScreen() {
    val projectRepository = remember { ProjectRepository() }
    val viewModel: ProjectListViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProjectListViewModel(projectRepository) as T
            }
        }
    )

    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        bottomBar = { NavBar() },
        floatingActionButton = { NewProjectFloatingButton() }
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
                        if (isLoading) {
                            repeat(3) {
                                ProjectCard(null)
                            }
                        } else {
                            projects.forEach { project ->
                                ProjectCard(project)
                            }
                        }
                    }
                }
            }
        }
    }
}
