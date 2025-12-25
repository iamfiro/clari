package com.iamfiro.clari.screen.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.R
import com.iamfiro.clari.core.Repository.ProjectRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.ConfirmBottomSheet
import com.iamfiro.clari.core.ui.theme.Dimens

@Composable
fun ProjectCreateScreen() {
    val backStack = LocalNavBackStack.current
    val projectRepository = remember { ProjectRepository() }
    val viewModel: ProjectCreateViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProjectCreateViewModel(projectRepository) as T
            }
        }
    )

    val projectName by viewModel.projectName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showConfirmSheet by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { backStack.removeLastOrNull() },
                    modifier = Modifier.offset(x = (-14).dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_left),
                        contentDescription = "Back"
                    )
                }
                Text(
                    "프로젝트 생성",
                    style = MaterialTheme.typography.titleLarge,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = projectName,
                onValueChange = { viewModel.updateProjectName(it) },
                label = { Text("프로젝트 이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showConfirmSheet = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = projectName.isNotBlank() && !isLoading,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("생성하기")
            }
        }
    }

    ConfirmBottomSheet(
        visible = showConfirmSheet,
        onDismiss = { showConfirmSheet = false },
        title = "프로젝트 생성",
        message = "정말 생성하시겠습니까?",
        confirmText = "생성",
        cancelText = "취소",
        onConfirm = {
            viewModel.createProject(
                onSuccess = {
                    backStack.removeLastOrNull()
                },
                onError = {
                    // TODO: 에러 처리 (Toast 등)
                }
            )
        }
    )
}
