package com.iamfiro.clari.screen.recording.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.component.HeaderWithBackButton
import com.iamfiro.clari.core.ui.component.SearchBar
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCard
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCardSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingProjectSelectionScreen() {
    val backStack = LocalNavBackStack.current

    val viewModel: RecordingProjectSelectionViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { HeaderWithBackButton("녹음을 시작하기 전에\n프로젝트를 선택해주세요") }

                    item { Spacer(Modifier.height(2.dp)) }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.startWithoutProject()
                                    backStack.add(Screen.LanguageSelectScreen(projectId = ""))
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shell),
                                        "none",
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Text("프로젝트 없이 시작하기", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            SearchBar(uiState.searchQuery, { viewModel.updateSearchQuery(it) })

                            if (uiState.isLoading) {
                                ProjectCardSkeleton()
                            } else {
                                uiState.filteredProjects.forEach { project ->
                                    ProjectCard(
                                        project = project,
                                        isSelected = uiState.selectedProject?.id == project.id,
                                        onClick = { viewModel.selectProject(project) }
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(100.dp))
                    }
                }
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
                        uiState.selectedProject?.let { project ->
                            backStack.add(Screen.LanguageSelectScreen(project.id))
                        }
                    },
                    enabled = uiState.selectedProject != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        if (uiState.selectedProject != null) "다음" else "프로젝트를 선택해주세요",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
