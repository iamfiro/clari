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
import com.iamfiro.clari.core.service.ConnectionState
import com.iamfiro.clari.feature.note.component.RecordingControl
import com.iamfiro.clari.feature.note.component.RecordingHeader
import com.iamfiro.clari.feature.note.component.TranscribeContainer
import kotlinx.coroutines.launch

private const val TAG = "RecordingScreen"

@Composable
fun RecordingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: RecordingViewModel = viewModel(
        factory = RecordingViewModelFactory(context.applicationContext)
    )

    val isRecording by viewModel.isRecording.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val transcriptItems by viewModel.transcriptItems.collectAsState()

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
        Log.d(TAG, "========== 권한 요청 결과 ==========")
        Log.d(TAG, "권한 승인: $isGranted")
        hasPermission = isGranted
        if (isGranted) {
            Log.d(TAG, "권한 승인됨 - 녹음 시작")
            viewModel.startRecording()
        } else {
            Log.w(TAG, "권한 거부됨")
            scope.launch {
                snackbarHostState.showSnackbar("마이크 권한이 필요합니다")
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "========== 화면 진입 - 자동 시작 ==========")
        Log.d(TAG, "현재 권한 상태: $hasPermission")
        
        if (!hasPermission) {
            Log.d(TAG, "권한 없음 - 권한 요청 시작")
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            Log.d(TAG, "권한 있음 - 녹음 바로 시작")
            viewModel.startRecording()
        }
    }

    LaunchedEffect(connectionState) {
        Log.d(TAG, "연결 상태 변경: $connectionState")
        when (connectionState) {
            is ConnectionState.Error -> {
                val errorMsg = (connectionState as ConnectionState.Error).message
                Log.e(TAG, "연결 오류: $errorMsg")
                snackbarHostState.showSnackbar("연결 오류: $errorMsg")
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "========== 화면 종료 ==========")
            if (isRecording) {
                Log.d(TAG, "녹음 중지")
                viewModel.stopRecording()
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RecordingHeader()
            Box(modifier = Modifier.weight(1f)) {
                TranscribeContainer(
                    transcriptItems = transcriptItems,
                    partialText = partialText
                )
            }
            RecordingControl(
                isRecording = isRecording,
                elapsedTime = viewModel.formatElapsedTime(elapsedSeconds),
                connectionState = connectionState,
                onToggleRecording = {
                    Log.d(TAG, "========== 녹음 버튼 클릭 ==========")
                    Log.d(TAG, "현재 녹음 상태: $isRecording")
                    Log.d(TAG, "권한 상태: $hasPermission")
                    
                    if (!hasPermission) {
                        Log.d(TAG, "권한 요청 시작")
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        Log.d(TAG, "녹음 토글")
                        viewModel.toggleRecording()
                    }
                }
            )
        }
    }
}
