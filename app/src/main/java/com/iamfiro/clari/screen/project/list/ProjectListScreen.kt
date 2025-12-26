package com.iamfiro.clari.screen.project.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.NewProjectFloatingButton
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCard
import com.iamfiro.clari.feature.project.component.projectCard.ProjectCardSkeleton

@Composable
fun ProjectListScreen() {
    val viewModel: ProjectListViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()
    val backStack = LocalNavBackStack.current

    HandleNavigationEvents(viewModel.navigationEvent, backStack)

    Box {
        Scaffold(
            bottomBar = { NavBar() },
            floatingActionButton = {
                NewProjectFloatingButton(onClick = { viewModel.showMenuModal() })
            }
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                Header("프로젝트")

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 6.dp)
                ) {
                    if (uiState.isLoading) {
                        item { ProjectCardSkeleton() }
                    } else {
                        items(
                            items = uiState.projects,
                            key = { it.id }
                        ) { project ->
                            ProjectCard(project, onClick = { viewModel.openProject(project.id) })
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
                title = "프로젝트 생성",
                onClick = {
                    backStack.add(Screen.ProjectCreate)
                }
            ),
            BottomSheetMenuItem(
                title = "URL에서 가져오기",
                onClick = {
                    viewModel.showImportSheet()
                }
            )
        )
    )

    BottomSheetWithHeader(
        visible = uiState.showImportSheet,
        onDismiss = { viewModel.hideImportSheet() },
        title = "URL에서 가져오기",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "가져오기",
                enabled = uiState.importUrl.isNotBlank(),
                onClick = {
                    if (uiState.importUrl.isNotBlank()) {
                        // TODO: URL에서 키워드팩 가져오기 구현
                        viewModel.hideImportSheet()
                    }
                }
            )
        )
    ) {
        OutlinedTextField(
            value = uiState.importUrl,
            onValueChange = { viewModel.updateImportUrl(it) },
            label = { Text("URL 입력") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
