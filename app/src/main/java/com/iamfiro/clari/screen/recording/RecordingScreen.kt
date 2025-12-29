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
import com.iamfiro.clari.feature.note.component.RecordingControl
import com.iamfiro.clari.feature.note.component.RecordingHeader
import com.iamfiro.clari.feature.note.component.TranscribeContainer
import com.iamfiro.clari.feature.note.component.WordDeckSection
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

    val viewModel: RecordingViewModel = viewModel(
        factory = RecordingViewModelFactory(
            context.applicationContext,
            recordingRepository,
            languageCode,
            keywordPackIds,
            externalResourceIds
        )
    )

    val isRecording by viewModel.isRecording.collectAsState()
    val isCreatingSession by viewModel.isCreatingSession.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val transcriptItems by viewModel.transcriptItems.collectAsState()
    val detectedKeywords by viewModel.detectedKeywords.collectAsState()
    val detectedTerms by viewModel.detectedTerms.collectAsState()
    val shouldTriggerHaptic by viewModel.shouldTriggerHaptic.collectAsState()
    val error by viewModel.error.collectAsState()

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
                snackbarHostState.showSnackbar("마이크 권한이 필요합니다")
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
                snackbarHostState.showSnackbar("연결 오류: $errorMsg")
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
                        showCancelBottomSheet = true
                    },
                    onExitClick = {
                        showConfirmBottomSheet = true
                    }
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
                    }
                )
                
                RecordingControl(
                    isRecording = isRecording,
                    elapsedTime = viewModel.formatElapsedTime(elapsedSeconds),
                    connectionState = when (connectionState) {
                        is SessionConnectionState.Connected, 
                        is SessionConnectionState.Ready -> com.iamfiro.clari.core.service.ConnectionState.Connected
                        is SessionConnectionState.Connecting -> com.iamfiro.clari.core.service.ConnectionState.Connecting
                        is SessionConnectionState.Error -> com.iamfiro.clari.core.service.ConnectionState.Error((connectionState as SessionConnectionState.Error).message)
                        else -> com.iamfiro.clari.core.service.ConnectionState.Disconnected
                    },
                    isLoading = isCreatingSession || isSaving,
                    onToggleRecording = {
                        Log.d(TAG, "녹음 버튼 클릭")
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else if (isRecording) {
                            isSaving = true
                            viewModel.stopRecording { noteId ->
                                isSaving = false
                                if (noteId != null) {
                                    Log.d(TAG, "녹음 저장 완료, NoteDetail로 이동: $noteId")
                                    backStack.removeLastOrNull()
                                    backStack.add(Screen.NoteDetail(noteId))
                                } else {
                                    Log.e(TAG, "녹음 저장 실패")
                                    scope.launch {
                                        snackbarHostState.showSnackbar("녹음 저장에 실패했습니다")
                                    }
                                }
                            }
                        } else {
                            viewModel.toggleRecording()
                        }
                    }
                )
            }
        }

        ConfirmBottomSheet(
            visible = showConfirmBottomSheet,
            onDismiss = { showConfirmBottomSheet = false },
            title = "녹음을 저장하시겠어요?",
            message = "지금까지 녹음한 내용이 저장됩니다.",
            confirmText = "저장하기",
            cancelText = "취소",
            onConfirm = {
                isSaving = true
                viewModel.stopRecording { noteId ->
                    isSaving = false
                    if (noteId != null) {
                        Log.d(TAG, "녹음 저장 완료, NoteDetail로 이동: $noteId")
                        backStack.removeLastOrNull()
                        backStack.add(Screen.NoteDetail(noteId))
                    } else {
                        Log.e(TAG, "녹음 저장 실패")
                        scope.launch {
                            snackbarHostState.showSnackbar("녹음 저장에 실패했습니다")
                        }
                        backStack.removeLastOrNull()
                    }
                }
            },
            onCancel = {
                showConfirmBottomSheet = false
            }
        )

        ConfirmBottomSheet(
            visible = showCancelBottomSheet,
            onDismiss = { showCancelBottomSheet = false },
            title = "녹음을 취소하시겠어요?",
            message = "녹음한 내용이 모두 삭제됩니다.",
            confirmText = "취소하기",
            cancelText = "돌아가기",
            onConfirm = {
                viewModel.cancelRecording()
                backStack.removeLastOrNull()
            },
            onCancel = {
                showCancelBottomSheet = false
            }
        )
    }
}
