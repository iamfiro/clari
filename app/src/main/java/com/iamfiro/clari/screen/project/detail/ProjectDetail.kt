package com.iamfiro.clari.screen.project.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.project.component.AddConnectorBottomSheet
import com.iamfiro.clari.feature.project.component.AddWordBottomSheet
import com.iamfiro.clari.feature.project.component.AiWordGenerationBottomSheet
import com.iamfiro.clari.feature.project.component.DeleteConnectorBottomSheet
import com.iamfiro.clari.feature.project.component.DeleteProjectBottomSheet
import com.iamfiro.clari.feature.project.component.DeleteWordBottomSheet
import com.iamfiro.clari.feature.project.component.EditConnectorBottomSheet
import com.iamfiro.clari.feature.project.component.ProjectDetailActionButtons
import com.iamfiro.clari.feature.project.component.ProjectDetailBanner
import com.iamfiro.clari.feature.project.component.ProjectDetailConnectors
import com.iamfiro.clari.feature.project.component.ProjectDetailErrorState
import com.iamfiro.clari.feature.project.component.ProjectDetailHeader
import com.iamfiro.clari.feature.project.component.ProjectDetailLoadingState
import com.iamfiro.clari.feature.project.component.ProjectDetailWords
import com.iamfiro.clari.feature.project.component.SavedProjectInfoSection
import com.iamfiro.clari.feature.project.component.ShareProjectBottomSheet
import com.iamfiro.clari.feature.project.component.UnsaveProjectBottomSheet
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(projectId: String) {
    val projectRepository = remember { ProjectRepository.getInstance() }
    val viewModel: ProjectDetailViewModel = viewModel(
        key = projectId,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectDetailViewModel(projectRepository, projectId) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        android.util.Log.d("ProjectDetailScreen", "projectId 변경 감지: $projectId")
        viewModel.refresh()
    }
    val backStack = LocalNavBackStack.current
    val context = LocalContext.current

    var showAddWordSheet by remember { mutableStateOf(false) }
    var showAddConnectorSheet by remember { mutableStateOf(false) }
    var showEditConnectorSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showDeleteProjectSheet by remember { mutableStateOf(false) }
    var showDeleteWordSheet by remember { mutableStateOf(false) }
    var showDeleteConnectorSheet by remember { mutableStateOf(false) }
    var showAiWordGenerationSheet by remember { mutableStateOf(false) }
    var showUnsaveSheet by remember { mutableStateOf(false) }

    var wordName by remember { mutableStateOf("") }
    var wordMeaning by remember { mutableStateOf("") }
    var connectorName by remember { mutableStateOf("") }
    var connectorUrl by remember { mutableStateOf("") }
    var selectedConnectorType by remember { mutableStateOf<ProjectConnectorType?>(null) }
    var editingConnector by remember { mutableStateOf<ProjectConnector?>(null) }
    var deletingWordName by remember { mutableStateOf("") }
    var deletingConnector by remember { mutableStateOf<ProjectConnector?>(null) }
    var shareLink by remember { mutableStateOf("") }
    var aiWordTopic by remember { mutableStateOf("") }
    var aiWordCount by remember { mutableStateOf("") }

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

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && uiState.project == null) {
                ProjectDetailLoadingState()
            } else if (uiState.project == null) {
                ProjectDetailErrorState(
                    errorMessage = uiState.error,
                    onRetryClick = { viewModel.refresh() }
                )
            } else {
                val project = uiState.project!!
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ProjectDetailBanner(
                            thumbnail = project.thumbnail,
                            onBannerClick = if (uiState.canEdit) {
                                {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            } else null,
                            onShareClick = if (uiState.isOwned) {{ showShareSheet = true }} else null,
                            onDeleteClick = if (uiState.canEdit) {{ showDeleteProjectSheet = true }} else null
                        )
                    }

                    item {
                        ProjectDetailHeader(
                            projectName = project.name,
                            createdAt = "2025년 12월 25일 생성"
                        )
                    }
                    
                    if (uiState.isSaved && !uiState.isOwned) {
                        item {
                            SavedProjectInfoSection(
                                publisherName = project.publisherName,
                                onUnsave = { showUnsaveSheet = true },
                                isUnsaving = uiState.isUnsaving,
                                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
                            )
                        }
                    }

                    item {
                        ProjectDetailConnectors(
                            connectors = project.connector,
                            onAddClick = if (uiState.canEdit) {{ showAddConnectorSheet = true }} else null,
                            onEditClick = if (uiState.canEdit) { { connector ->
                                editingConnector = connector
                                connectorName = connector.name
                                connectorUrl = connector.url
                                showEditConnectorSheet = true
                            } } else null,
                            onDeleteClick = if (uiState.canEdit) { { connector ->
                                deletingConnector = connector
                                showDeleteConnectorSheet = true
                            } } else null
                        )
                    }

                    item {
                        ProjectDetailWords(
                            words = project.word,
                            onWordLongPress = if (uiState.canEdit) { { wordName ->
                                deletingWordName = wordName
                                showDeleteWordSheet = true
                            } } else null
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(130.dp))
                    }
                }

                if (uiState.canEdit) {
                    ProjectDetailActionButtons(
                        onAddWordClick = { showAddWordSheet = true },
                        onAddAiWordClick = { showAiWordGenerationSheet = true },
                        isAiGenerating = uiState.isAiLoading,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    if (uiState.canEdit) {
        AddWordBottomSheet(
            visible = showAddWordSheet,
            onDismiss = {
                showAddWordSheet = false
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
            aiSuggestions = uiState.aiSuggestions,
            isAiLoading = uiState.isAiLoading,
            onGetAiSuggestions = { name -> viewModel.getAiSuggestions(name) },
            onGenerateDescription = { keywordName ->
                projectRepository.generateWordDescription(keywordName)
            }
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

        LaunchedEffect(uiState.project?.connector?.size) {
            if (!uiState.isAddingConnector && showAddConnectorSheet && connectorUrl.isNotEmpty()) {
                showAddConnectorSheet = false
                selectedConnectorType = null
                connectorName = ""
                connectorUrl = ""
            }
        }

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
            isAdding = uiState.isAddingConnector,
            onAdd = {
                selectedConnectorType?.let { type ->
                    viewModel.addConnector(type, connectorName, connectorUrl)
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

        AiWordGenerationBottomSheet(
            visible = showAiWordGenerationSheet,
            onDismiss = {
                if (!uiState.isAiLoading) {
                    showAiWordGenerationSheet = false
                    aiWordTopic = ""
                    aiWordCount = ""
                    viewModel.clearAiGeneratedWords()
                }
            },
            topic = aiWordTopic,
            onTopicChange = { aiWordTopic = it },
            count = aiWordCount,
            onCountChange = { aiWordCount = it },
            isGenerating = uiState.isAiLoading,
            generatedWords = uiState.aiGeneratedWords,
            onGenerate = {
                val count = aiWordCount.toIntOrNull() ?: 10
                viewModel.generateAiWords(aiWordTopic, count)
            },
            onDeleteWord = { word ->
                viewModel.removeAiGeneratedWord(word)
            },
            onAddWords = {
                viewModel.addAiGeneratedWordsToProject {
                    showAiWordGenerationSheet = false
                    aiWordTopic = ""
                    aiWordCount = ""
                    viewModel.clearAiGeneratedWords()
                }
            }
        )
    }

    if (uiState.isOwned) {
        ShareProjectBottomSheet(
            visible = showShareSheet,
            onDismiss = { showShareSheet = false },
            shareLink = shareLink,
            isPublic = uiState.isPublic,
            isTogglingPublic = uiState.isTogglingPublic,
            onTogglePublic = { viewModel.togglePublic() },
            onCopyLink = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("공유 링크", shareLink)
                clipboard.setPrimaryClip(clip)
                showShareSheet = false
            }
        )
    }
    
    UnsaveProjectBottomSheet(
        visible = showUnsaveSheet,
        onDismiss = { showUnsaveSheet = false },
        onConfirm = {
            viewModel.cloudUnsave {
                showUnsaveSheet = false
                backStack.removeLastOrNull()
            }
        }
    )
}
