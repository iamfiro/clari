package com.iamfiro.clari.screen.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.HeaderWithBackButton
import com.iamfiro.clari.core.ui.component.SearchBar
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.ProjectCard

@Composable
fun BeforeRecordingScreen() {
    val backStack = LocalNavBackStack.current
    val projectRepository = remember { ProjectRepository() }
    val viewModel: BeforeRecordingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BeforeRecordingViewModel(projectRepository) as T
            }
        }
    )

    val filteredProjects by viewModel.filteredProjects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(selectedProject) {
        if (selectedProject != null) {
            listState.animateScrollToItem(0)
        }
    }

    val sortedProjects = remember(filteredProjects, selectedProject) {
        if (selectedProject != null) {
            listOf(selectedProject!!) + filteredProjects.filter { it.id != selectedProject!!.id }
        } else {
            filteredProjects
        }
    }

    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { HeaderWithBackButton("녹음을 시작하기 전에\n단어 팩을 선택해주세요") }

                item { Spacer(Modifier.height(4.dp)) }

                item {
                    SearchBar(
                        searchQuery,
                        { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                    )
                }

                if (isLoading) {
                    items(3) {
                        ProjectCard(
                            null,
                            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                        )
                    }
                } else {
                    items(
                        items = sortedProjects,
                        key = { it.id }
                    ) { project ->
                        ProjectCard(
                            project = project,
                            isSelected = project.id == selectedProject?.id,
                            onClick = {
                                if (project.id == selectedProject?.id) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.selectProject(project)
                                }
                            },
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = Dimens.ScreenPadding)
                        )
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 1.dp)
                    .align(Alignment.BottomCenter)
                    .height(130.dp)
                    .padding(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Button(
                    onClick = {
                        selectedProject?.let { project ->
                            backStack.add(Screen.Recording(project.id))
                        }
                    },
                    enabled = selectedProject != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        if (selectedProject != null) "눌러서 시작하기" else "단어 팩을 선택해주세요",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
