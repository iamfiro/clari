package com.iamfiro.clari.screen.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.core.ui.LocalCurrentScreen
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.BottomSheetAction
import com.iamfiro.clari.core.ui.component.BottomSheetMenuItem
import com.iamfiro.clari.core.ui.component.BottomSheetWithHeader
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.MenuBottomSheet
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.ProjectCard
import kotlinx.coroutines.launch

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
    val currentScreen = LocalCurrentScreen.current
    val backStack = LocalNavBackStack.current
    val scope = rememberCoroutineScope()

    var showMenuModal by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var importLink by remember { mutableStateOf("") }

    LaunchedEffect(currentScreen) {
        if (currentScreen is Screen.ProjectList) {
            viewModel.refresh()
        }
    }

    Box {
        Scaffold(
            bottomBar = { NavBar() },
            floatingActionButton = { 
                NewProjectFloatingButton(onClick = { showMenuModal = true }) 
            }
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                Header("프로젝트")

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 6.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isLoading) {
                                repeat(3) {
                                    ProjectCard(null)
                                }
                            } else {
                                projects.forEach { project ->
                                    ProjectCard(project, onClick = { backStack.add(Screen.ProjectDetail(project.id)) })
                                }
                            }
                        }
                    }
                }
            }
        }

        MenuBottomSheet(
            visible = showMenuModal,
            onDismiss = { showMenuModal = false },
            items = listOf(
                BottomSheetMenuItem(
                    title = "프로젝트 생성",
                    onClick = {
                        backStack.add(Screen.ProjectCreate)
                    }
                ),
                BottomSheetMenuItem(
                    title = "프로젝트 불러오기",
                    onClick = {
                        showImportSheet = true
                    }
                )
            )
        )

        BottomSheetWithHeader(
            visible = showImportSheet,
            onDismiss = { 
                showImportSheet = false
                importLink = ""
            },
            title = "프로젝트 불러오기",
            actions = listOf(
                BottomSheetAction.Primary(
                    text = "불러오기",
                    enabled = importLink.isNotBlank(),
                    onClick = {
                        if (importLink.isNotBlank()) {
                            scope.launch {
                                projectRepository.importProjectByLink(importLink)
                                viewModel.refresh()
                            }
                            showImportSheet = false
                            importLink = ""
                        }
                    }
                )
            )
        ) {
            OutlinedTextField(
                value = importLink,
                onValueChange = { importLink = it },
                label = { Text("링크 입력") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
