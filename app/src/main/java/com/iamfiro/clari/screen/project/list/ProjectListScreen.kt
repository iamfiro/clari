package com.iamfiro.clari.screen.project.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.ui.HandleNavigationEvents
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.BottomSheetAction
import com.iamfiro.clari.core.ui.component.BottomSheetMenuItem
import com.iamfiro.clari.core.ui.component.BottomSheetWithHeader
import com.iamfiro.clari.core.ui.component.Header
import com.iamfiro.clari.core.ui.component.MenuBottomSheet
import com.iamfiro.clari.core.ui.component.NavBar
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.NewProjectFloatingButton
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCard
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCardSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen() {
    val viewModel: ProjectListViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()
    val backStack = LocalNavBackStack.current

    HandleNavigationEvents(viewModel.navigationEvent, backStack)

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box {
        Scaffold(
            bottomBar = { NavBar() },
            floatingActionButton = {
                NewProjectFloatingButton(onClick = { viewModel.showMenuModal() })
            }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Header("Projects")

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.ScreenPadding, vertical = 6.dp)
                    ) {
                        if (uiState.isLoading) {
                            item { ProjectCardSkeleton() }
                        } else {
                            if (uiState.ownedProjects.isNotEmpty()) {
                                item {
                                    SectionTitle("My Projects")
                                }
                                items(
                                    items = uiState.ownedProjects,
                                    key = { it.id }
                                ) { project ->
                                    ProjectCard(project, onClick = { viewModel.openProject(project.id) })
                                }
                            }
                            
                            if (uiState.savedProjects.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SectionTitle("Saved Projects")
                                }
                                items(
                                    items = uiState.savedProjects,
                                    key = { it.id }
                                ) { project ->
                                    ProjectCard(project, onClick = { viewModel.openProject(project.id) })
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    MenuBottomSheet(
        visible = uiState.showMenuModal,
        onDismiss = { viewModel.hideMenuModal() },
        items = listOf(
            BottomSheetMenuItem(
                title = "Create Project",
                onClick = {
                    backStack.add(Screen.ProjectCreate)
                }
            ),
            BottomSheetMenuItem(
                title = "Import from URL",
                onClick = {
                    viewModel.showImportSheet()
                }
            )
        )
    )

    BottomSheetWithHeader(
        visible = uiState.showImportSheet,
        onDismiss = { viewModel.hideImportSheet() },
        title = "Import from URL",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "Import",
                enabled = uiState.importUrl.isNotBlank(),
                onClick = {
                    if (uiState.importUrl.isNotBlank()) {
                        viewModel.hideImportSheet()
                    }
                }
            )
        )
    ) {
        OutlinedTextField(
            value = uiState.importUrl,
            onValueChange = { viewModel.updateImportUrl(it) },
            label = { Text("Enter URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
