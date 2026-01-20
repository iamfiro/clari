package com.iamfiro.clari.screen.recording

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.repository.RecordingRepository
import com.iamfiro.clari.core.service.SessionConnectionState
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.ConfirmBottomSheet
import com.iamfiro.clari.feature.note.component.DetectedTerm
import com.iamfiro.clari.feature.note.component.RecordingControl
import com.iamfiro.clari.feature.note.component.RecordingHeader
import com.iamfiro.clari.feature.note.component.TranscribeContainer
import com.iamfiro.clari.feature.note.component.WordDeckSection
import com.iamfiro.clari.feature.note.component.WordDetailModal
import kotlinx.coroutines.launch

private const val TAG = "RecordingScreen"

@Composable
fun RecordingScreen(
    projectId: String,
    languageCode: String,
    keywordPackIds: List<String> = emptyList(),
    externalResourceIds: List<String> = emptyList()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val recordingRepository = remember { RecordingRepository.getInstance() }
    val backStack = LocalNavBackStack.current
    
    var showConfirmBottomSheet by remember { mutableStateOf(false) }
    var showCancelBottomSheet by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showWordDetailModal by remember { mutableStateOf(false) }
    var selectedTerm by remember { mutableStateOf<DetectedTerm?>(null) }

    val viewModel: RecordingViewModel = viewModel(
        key = "$projectId-$languageCode-${keywordPackIds.joinToString()}",
        factory = RecordingViewModelFactory(
            context.applicationContext,
            recordingRepository,
            languageCode,
            keywordPackIds,
            externalResourceIds
        )
    )

    val isRecording by viewModel.isRecording.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isCreatingSession by viewModel.isCreatingSession.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val transcriptItems by viewModel.transcriptItems.collectAsState()
    val detectedKeywords by viewModel.detectedKeywords.collectAsState()
    val detectedTerms by viewModel.detectedTerms.collectAsState()
    val shouldTriggerHaptic by viewModel.shouldTriggerHaptic.collectAsState()
    val error by viewModel.error.collectAsState()

    DisposableEffect(languageCode) {
        Log.d(TAG, "========== RecordingScreen 진입 (language: $languageCode) ==========")
        onDispose {
            Log.d(TAG, "========== RecordingScreen 종료 (language: $languageCode) ==========")
        }
    }

    var hasPermission by remember {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "초기 권한 상태: $granted")
        mutableStateOf(granted)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "권한 요청 결과: $isGranted")
        hasPermission = isGranted
        if (isGranted) {
            viewModel.startRecording()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission is required")
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "========== 화면 진입 - 자동 시작 ==========")
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.startRecording()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(connectionState) {
        Log.d(TAG, "연결 상태 변경: $connectionState")
        when (connectionState) {
            is SessionConnectionState.Error -> {
                val errorMsg = (connectionState as SessionConnectionState.Error).message
                snackbarHostState.showSnackbar("Connection error: $errorMsg")
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "========== 화면 종료 ==========")
            if (isRecording) {
                viewModel.cancelRecording()
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                RecordingHeader(
                    onBackClick = {
                        viewModel.pauseRecording()
                        showCancelBottomSheet = true
                    },
                    onExitClick = {
                        viewModel.pauseRecording()
                        if (elapsedSeconds < 10) {
                            showCancelBottomSheet = true
                        } else {
                            showConfirmBottomSheet = true
                        }
                    },
                    isPaused = isPaused
                )
                Box(modifier = Modifier.weight(1f)) {
                    TranscribeContainer(
                        transcriptItems = transcriptItems.map { 
                            com.iamfiro.clari.core.service.model.TranscriptItem(
                                id = it.id,
                                committedText = it.text,
                                committedChunks = listOf(it.text),
                                formattedText = if (it.isFormatted) it.text else null,
                                formattedChunks = if (it.isFormatted) listOf(it.text) else null,
                                isFormatted = it.isFormatted
                            )
                        },
                        partialText = partialText?.let {
                            com.iamfiro.clari.core.service.model.SttResponse(
                                type = "partial",
                                text = it,
                                chunks = listOf(it)
                            )
                        }
                    )
                }
                WordDeckSection(
                    terms = detectedTerms,
                    shouldTriggerHaptic = shouldTriggerHaptic,
                    onHapticTriggered = { viewModel.onHapticTriggered() },
                    onCardClick = { term ->
                        Log.d(TAG, "탐지된 용어 클릭: ${term.keyword.name}")
                    },
                    onCardLongPress = { term ->
                        selectedTerm = term
                        showWordDetailModal = true
                    }
                )
                
                RecordingControl(
                    isRecording = isRecording,
                    isPaused = isPaused,
                    elapsedTime = viewModel.formatElapsedTime(elapsedSeconds),
                    connectionState = when (connectionState) {
                        is SessionConnectionState.Connected, 
                        is SessionConnectionState.Ready -> com.iamfiro.clari.core.service.ConnectionState.Connected
                        is SessionConnectionState.Connecting -> com.iamfiro.clari.core.service.ConnectionState.Connecting
                        is SessionConnectionState.Error -> com.iamfiro.clari.core.service.ConnectionState.Error((connectionState as SessionConnectionState.Error).message)
                        else -> com.iamfiro.clari.core.service.ConnectionState.Disconnected
                    },
                    isLoading = isCreatingSession || isSaving,
                    onPauseToggle = {
                        Log.d(TAG, "일시정지/재개 버튼 클릭")
                        if (isPaused) {
                            viewModel.resumeRecording()
                        } else {
                            viewModel.pauseRecording()
                        }
                    }
                )
            }
        }

        ConfirmBottomSheet(
            visible = showConfirmBottomSheet,
            onDismiss = {
                showConfirmBottomSheet = false
                viewModel.resumeRecording()
            },
            title = "Save recording?",
            message = "Your recording will be saved.",
            confirmText = "Save",
            cancelText = "Cancel",
            onConfirm = {
                isSaving = true
                viewModel.stopRecording { noteId ->
                    isSaving = false
                    if (noteId != null) {
                        Log.d(TAG, "녹음 저장 완료, NoteDetail로 이동: $noteId")
                        // 녹음 관련 화면들을 모두 제거하고 Home만 남긴 후 NoteDetail로 이동
                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                        backStack.add(Screen.NoteDetail(noteId))
                    } else {
                        Log.e(TAG, "녹음 저장 실패")
                        scope.launch {
                            snackbarHostState.showSnackbar("Failed to save recording")
                        }
                        backStack.removeLastOrNull()
                    }
                }
            },
            onCancel = {
                showConfirmBottomSheet = false
                viewModel.resumeRecording()
            }
        )

        ConfirmBottomSheet(
            visible = showCancelBottomSheet,
            onDismiss = {
                showCancelBottomSheet = false
                viewModel.resumeRecording()
            },
            title = if (elapsedSeconds < 10) "Recording is less than 10 seconds" else "Cancel recording?",
            message = if (elapsedSeconds < 10) "Recordings under 10 seconds will not be saved. Continue anyway?" else "Your recording will be deleted.",
            confirmText = if (elapsedSeconds < 10) "Delete" else "Cancel Recording",
            cancelText = "Go Back",
            onConfirm = {
                viewModel.cancelRecording()
                backStack.removeLastOrNull()
            },
            onCancel = {
                showCancelBottomSheet = false
                viewModel.resumeRecording()
            }
        )
        
        WordDetailModal(
            visible = showWordDetailModal,
            term = selectedTerm,
            onDismiss = {
                showWordDetailModal = false
                selectedTerm = null
            }
        )
    }
}
