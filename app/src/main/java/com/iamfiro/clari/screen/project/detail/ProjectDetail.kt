package com.iamfiro.clari.screen.project.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.iamfiro.clari.R
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.AddConnectorBottomSheet
import com.iamfiro.clari.feature.project.component.AddWordBottomSheet
import com.iamfiro.clari.feature.project.component.ConnectorCard
import com.iamfiro.clari.feature.project.component.DeleteConnectorBottomSheet
import com.iamfiro.clari.feature.project.component.DeleteProjectBottomSheet
import com.iamfiro.clari.feature.project.component.DeleteWordBottomSheet
import com.iamfiro.clari.feature.project.component.EditConnectorBottomSheet
import com.iamfiro.clari.feature.project.component.ShareProjectBottomSheet
import com.iamfiro.clari.feature.project.component.WordCard
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(projectId: String) {
    val projectRepository = remember { ProjectRepository.getInstance() }
    val viewModel: ProjectDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectDetailViewModel(projectRepository, projectId) as T
            }
        }
    )

    val project by viewModel.project.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val backStack = LocalNavBackStack.current
    val context = LocalContext.current

    var showAddWordSheet by remember { mutableStateOf(false) }
    var showAddConnectorSheet by remember { mutableStateOf(false) }
    var showEditConnectorSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showDeleteProjectSheet by remember { mutableStateOf(false) }
    var showDeleteWordSheet by remember { mutableStateOf(false) }
    var showDeleteConnectorSheet by remember { mutableStateOf(false) }

    var wordName by remember { mutableStateOf("") }
    var wordMeaning by remember { mutableStateOf("") }
    var connectorName by remember { mutableStateOf("") }
    var connectorUrl by remember { mutableStateOf("") }
    var selectedConnectorType by remember { mutableStateOf<ProjectConnectorType?>(null) }
    var editingConnector by remember { mutableStateOf<ProjectConnector?>(null) }
    var deletingWordName by remember { mutableStateOf("") }
    var deletingConnector by remember { mutableStateOf<ProjectConnector?>(null) }
    var shareLink by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateBannerImage(it)
        }
    }

    LaunchedEffect(showShareSheet) {
        if (showShareSheet) {
            shareLink = viewModel.getShareLink()
        }
    }

    Scaffold { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("로딩 중...")
                }
            } else if (project == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("프로젝트를 찾을 수 없습니다.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Box {
                            AsyncImage(
                                model = project!!.thumbnail,
                                contentDescription = "배너",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.sample_banner),
                                error = painterResource(R.drawable.sample_banner)
                            )

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.ScreenPadding, 40.dp)
                            ) {
                                IconButton(
                                    onClick = { backStack.removeLastOrNull() },
                                    modifier = Modifier.offset(x = (-14).dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_left),
                                        contentDescription = "뒤로가기",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IconButton(
                                        onClick = { showShareSheet = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.share),
                                            contentDescription = "공유",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { showDeleteProjectSheet = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.trash),
                                            contentDescription = "삭제",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(Dimens.ScreenPadding)
                        ) {
                            Text(
                                project!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "2025년 12월 25일 생성",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SectionTitle("외부 연결")
                                OutlinedButton(
                                    onClick = { showAddConnectorSheet = true },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("추가", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            project!!.connector?.forEach { connector ->
                                ConnectorCard(
                                    connector = connector,
                                    onEdit = {
                                        editingConnector = connector
                                        connectorName = connector.name
                                        connectorUrl = connector.url
                                        showEditConnectorSheet = true
                                    },
                                    onDelete = {
                                        deletingConnector = connector
                                        showDeleteConnectorSheet = true
                                    }
                                )
                            }
                            if (project!!.connector.isNullOrEmpty()) {
                                Text(
                                    "외부 연결이 없습니다",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(
                                start = Dimens.ScreenPadding,
                                end = Dimens.ScreenPadding,
                                top = 24.dp
                            )
                        ) {
                            SectionTitle("단어")
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        if (project!!.word.isEmpty()) {
                            Text(
                                "단어가 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                            ) {
                                project!!.word.forEach { word ->
                                    WordCard(
                                        word = word,
                                        modifier = Modifier
                                            .width(160.dp)
                                            .pointerInput(word.name) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        deletingWordName = word.name
                                                        showDeleteWordSheet = true
                                                    }
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(130.dp))
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
                        onClick = { showAddWordSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(
                            "단어 추가",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    AddWordBottomSheet(
        visible = showAddWordSheet,
        onDismiss = {
            showAddWordSheet = false
            wordName = ""
            wordMeaning = ""
            viewModel.clearAiSuggestions()
        },
        wordName = wordName,
        wordMeaning = wordMeaning,
        onWordNameChange = { wordName = it },
        onWordMeaningChange = { wordMeaning = it },
        onAdd = {
            viewModel.addWord(wordName, wordMeaning)
            showAddWordSheet = false
            wordName = ""
            wordMeaning = ""
            viewModel.clearAiSuggestions()
        },
        aiSuggestions = aiSuggestions,
        isAiLoading = isAiLoading,
        onGetAiSuggestions = { name -> viewModel.getAiSuggestions(name) }
    )

    DeleteWordBottomSheet(
        visible = showDeleteWordSheet,
        onDismiss = { showDeleteWordSheet = false },
        onConfirm = {
            viewModel.removeWord(deletingWordName)
            showDeleteWordSheet = false
            deletingWordName = ""
        }
    )

    AddConnectorBottomSheet(
        visible = showAddConnectorSheet,
        onDismiss = {
            showAddConnectorSheet = false
            selectedConnectorType = null
            connectorName = ""
            connectorUrl = ""
        },
        selectedConnectorType = selectedConnectorType,
        connectorName = connectorName,
        connectorUrl = connectorUrl,
        onSelectedConnectorTypeChange = { selectedConnectorType = it },
        onConnectorNameChange = { connectorName = it },
        onConnectorUrlChange = { connectorUrl = it },
        onAdd = {
            selectedConnectorType?.let { type ->
                viewModel.addConnector(type, connectorName, connectorUrl)
                showAddConnectorSheet = false
                selectedConnectorType = null
                connectorName = ""
                connectorUrl = ""
            }
        }
    )

    editingConnector?.let { connector ->
        EditConnectorBottomSheet(
            visible = showEditConnectorSheet,
            onDismiss = {
                showEditConnectorSheet = false
                editingConnector = null
                connectorName = ""
                connectorUrl = ""
            },
            connector = connector,
            connectorName = connectorName,
            connectorUrl = connectorUrl,
            onConnectorNameChange = { connectorName = it },
            onConnectorUrlChange = { connectorUrl = it },
            onDelete = {
                showEditConnectorSheet = false
                deletingConnector = connector
                showDeleteConnectorSheet = true
            },
            onSave = {
                val newConnector = connector.copy(
                    name = connectorName,
                    url = connectorUrl
                )
                viewModel.updateConnector(connector, newConnector)
                showEditConnectorSheet = false
                editingConnector = null
                connectorName = ""
                connectorUrl = ""
            }
        )
    }

    deletingConnector?.let { connector ->
        DeleteConnectorBottomSheet(
            visible = showDeleteConnectorSheet,
            onDismiss = { showDeleteConnectorSheet = false },
            onConfirm = {
                viewModel.removeConnector(connector)
                showDeleteConnectorSheet = false
                deletingConnector = null
            }
        )
    }

    ShareProjectBottomSheet(
        visible = showShareSheet,
        onDismiss = { showShareSheet = false },
        shareLink = shareLink,
        onCopyLink = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("공유 링크", shareLink)
            clipboard.setPrimaryClip(clip)
            showShareSheet = false
        }
    )

    DeleteProjectBottomSheet(
        visible = showDeleteProjectSheet,
        onDismiss = { showDeleteProjectSheet = false },
        onConfirm = {
            viewModel.deleteProject {
                backStack.removeLastOrNull()
            }
            showDeleteProjectSheet = false
        }
    )
}
