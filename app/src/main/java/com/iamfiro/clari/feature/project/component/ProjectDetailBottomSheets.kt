package com.iamfiro.clari.feature.project.component

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import coil.size.Size
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.component.BottomSheet
import kotlinx.coroutines.launch
import com.iamfiro.clari.core.ui.component.BottomSheetAction
import com.iamfiro.clari.core.ui.component.BottomSheetConfig
import com.iamfiro.clari.core.ui.component.BottomSheetWithHeader
import com.iamfiro.clari.core.ui.component.ConfirmBottomSheet
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType

@Composable
fun AddWordBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    wordName: String,
    wordMeaning: String,
    onWordNameChange: (String) -> Unit,
    onWordMeaningChange: (String) -> Unit,
    onAdd: () -> Unit,
    aiSuggestions: List<String> = emptyList(),
    isAiLoading: Boolean = false,
    onGetAiSuggestions: ((String) -> Unit)? = null,
    onGenerateDescription: (suspend (String) -> Result<List<String>>)? = null
) {
    var isGeneratingDescription by remember { mutableStateOf(false) }
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "단어 추가",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "추가",
                enabled = wordName.isNotBlank() && wordMeaning.isNotBlank(),
                onClick = onAdd
            )
        )
    ) {
        OutlinedTextField(
            value = wordName,
            onValueChange = onWordNameChange,
            label = { Text("단어 이름") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (onGetAiSuggestions != null && wordName.isNotBlank()) {
                    IconButton(
                        onClick = { onGetAiSuggestions(wordName) },
                        enabled = !isAiLoading
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "AI 제안 가져오기",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wordMeaning,
            onValueChange = onWordMeaningChange,
            label = { Text("단어 뜻") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(12.dp))

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var generatedSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
        val speechRecognizerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val resultCode = result.resultCode
            val data = result.data
            
            if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                results?.firstOrNull()?.let { recognizedText ->
                    onWordMeaningChange(recognizedText)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "단어 뜻을 말씀해주세요")
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        speechRecognizerLauncher.launch(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.mic), "", modifier = Modifier.size(16.dp))
                    Text("음성 인식", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    if (wordName.isBlank()) {
                        Toast.makeText(context, "단어 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else if (onGenerateDescription != null) {
                        isGeneratingDescription = true
                        scope.launch {
                            onGenerateDescription(wordName)
                                .onSuccess { suggestions ->
                                    generatedSuggestions = suggestions
                                    isGeneratingDescription = false
                                }
                                .onFailure {
                                    Toast.makeText(context, "AI 생성에 실패했습니다", Toast.LENGTH_SHORT).show()
                                    generatedSuggestions = emptyList()
                                    isGeneratingDescription = false
                                }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                enabled = onGenerateDescription != null && !isGeneratingDescription
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isGeneratingDescription) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(painter = painterResource(R.drawable.sparkles), "", modifier = Modifier.size(16.dp))
                    }
                    Text("AI 생성", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (generatedSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "AI 생성 결과 (선택하세요)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                generatedSuggestions.forEachIndexed { index, suggestion ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                onWordMeaningChange(suggestion)
                                generatedSuggestions = emptyList()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteWordBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    
    ConfirmBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = "단어 삭제",
        message = "정말 이 단어를 삭제하시겠습니까?",
        confirmText = "삭제",
        cancelText = "취소",
        isDestructive = true,
        onConfirm = {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
            
            onConfirm()
        }
    )
}

@Composable
fun AddConnectorBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedConnectorType: ProjectConnectorType?,
    connectorName: String,
    connectorUrl: String,
    onSelectedConnectorTypeChange: (ProjectConnectorType?) -> Unit,
    onConnectorNameChange: (String) -> Unit,
    onConnectorUrlChange: (String) -> Unit,
    isAdding: Boolean = false,
    onAdd: () -> Unit
) {
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "외부 연결 추가",
        actions = listOf(
            BottomSheetAction.Primary(
                text = if (isAdding) "추가 중..." else "추가",
                enabled = selectedConnectorType != null && connectorName.isNotBlank() && connectorUrl.isNotBlank() && !isAdding,
                onClick = onAdd
            )
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedConnectorType == ProjectConnectorType.NOTION) {
                Button(
                    onClick = { onSelectedConnectorTypeChange(ProjectConnectorType.NOTION) },
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.notion),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notion")
                }
            } else {
                OutlinedButton(
                    onClick = { onSelectedConnectorTypeChange(ProjectConnectorType.NOTION) },
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.notion),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notion")
                }
            }
            if (selectedConnectorType == ProjectConnectorType.GDRIVE) {
                Button(
                    onClick = { onSelectedConnectorTypeChange(ProjectConnectorType.GDRIVE) },
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.gdrive),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GDrive")
                }
            } else {
                OutlinedButton(
                    onClick = { onSelectedConnectorTypeChange(ProjectConnectorType.GDRIVE) },
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.gdrive),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GDrive")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = connectorName,
            onValueChange = onConnectorNameChange,
            label = { Text("연결 이름") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = connectorUrl,
            onValueChange = onConnectorUrlChange,
            label = { Text("링크") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun EditConnectorBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    connector: ProjectConnector,
    connectorName: String,
    connectorUrl: String,
    onConnectorNameChange: (String) -> Unit,
    onConnectorUrlChange: (String) -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "외부 연결 수정",
        titleIcon = {
            Icon(
                painter = painterResource(
                    when (connector.type) {
                        ProjectConnectorType.NOTION -> R.drawable.notion
                        ProjectConnectorType.GDRIVE -> R.drawable.gdrive
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        actions = listOf(
            BottomSheetAction.Secondary(
                text = "삭제",
                onClick = onDelete,
                enabled = true
            ),
            BottomSheetAction.Primary(
                text = "저장",
                enabled = connectorName.isNotBlank() && connectorUrl.isNotBlank(),
                onClick = onSave
            )
        )
    ) {
        OutlinedTextField(
            value = connectorName,
            onValueChange = onConnectorNameChange,
            label = { Text("연결 이름") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = connectorUrl,
            onValueChange = onConnectorUrlChange,
            label = { Text("링크") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun DeleteConnectorBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = "외부 연결 삭제",
        message = "정말 이 연결을 삭제하시겠습니까?",
        confirmText = "삭제",
        cancelText = "취소",
        isDestructive = true,
        onConfirm = onConfirm
    )
}

@Composable
fun ShareProjectBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    shareLink: String,
    isPublic: Boolean,
    isTogglingPublic: Boolean = false,
    onTogglePublic: () -> Unit,
    onCopyLink: () -> Unit
) {
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "프로젝트 공유",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "링크 복사",
                enabled = isPublic,
                onClick = onCopyLink
            )
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "공개 설정",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (isPublic) "누구나 링크로 접근 가능" else "나만 볼 수 있음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                androidx.compose.material3.Switch(
                    checked = isPublic,
                    onCheckedChange = { onTogglePublic() },
                    enabled = !isTogglingPublic
                )
            }
            
            if (isPublic) {
                OutlinedTextField(
                    value = shareLink,
                    onValueChange = {},
                    label = { Text("공유 링크") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Text(
                        "프로젝트를 공개로 설정하면 링크가 생성됩니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun SavedProjectInfoSection(
    publisherName: String,
    onUnsave: () -> Unit,
    isUnsaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    "저장한 프로젝트",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "by $publisherName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            OutlinedButton(
                onClick = onUnsave,
                enabled = !isUnsaving,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isUnsaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("저장 해제", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun UnsaveProjectBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = "저장 해제",
        message = "이 프로젝트를 저장 목록에서 제거하시겠습니까?",
        confirmText = "저장 해제",
        cancelText = "취소",
        isDestructive = true,
        onConfirm = onConfirm
    )
}

@Composable
fun DeleteProjectBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = "프로젝트 삭제",
        message = "정말 이 프로젝트를 삭제하시겠습니까?",
        confirmText = "삭제",
        cancelText = "취소",
        isDestructive = true,
        onConfirm = onConfirm
    )
}

@Composable
fun AiAutofillBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    generatedWords: List<com.iamfiro.clari.feature.project.model.Word>,
    onGenerate: () -> Unit,
    onApply: () -> Unit
) {
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "AI 자동채우기",
        actions = listOf(
            BottomSheetAction.Secondary(
                text = if (isLoading) "생성 중..." else "키워드 생성",
                onClick = onGenerate,
                enabled = query.isNotBlank() && !isLoading
            ),
            BottomSheetAction.Primary(
                text = "추가하기",
                enabled = generatedWords.isNotEmpty() && !isLoading,
                onClick = onApply
            )
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "주제나 분야를 입력하면 AI가 관련 키워드를 생성합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("주제 입력") },
                placeholder = { Text("예: AWS 클라우드 서비스, 머신러닝 기초") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("키워드 생성 중...")
                }
            }
            
            if (generatedWords.isNotEmpty()) {
                Text(
                    "${generatedWords.size}개 키워드 생성됨",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    generatedWords.take(10).forEach { word ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    word.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    word.meaning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    if (generatedWords.size > 10) {
                        Text(
                            "외 ${generatedWords.size - 10}개 더...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiWordGenerationBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    topic: String,
    onTopicChange: (String) -> Unit,
    count: String,
    onCountChange: (String) -> Unit,
    isGenerating: Boolean,
    generatedWords: List<com.iamfiro.clari.feature.project.model.Word>,
    onGenerate: () -> Unit,
    onDeleteWord: (com.iamfiro.clari.feature.project.model.Word) -> Unit,
    onAddWords: () -> Unit
) {
    if (isGenerating) {
        BottomSheet(
            visible = visible,
            onDismiss = { /* 생성 중엔 닫기 불가 */ },
            config = BottomSheetConfig(
                dismissOnBackdropClick = false,
                enableSwipeToDismiss = false
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(24.dp))
                Text(
                    "단어를 생성중이에요...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "약 1분 소요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    } else if (generatedWords.isNotEmpty()) {
        BottomSheetWithHeader(
            visible = visible,
            onDismiss = onDismiss,
            title = "${generatedWords.size}개의 단어가 생성되었습니다",
            actions = listOf(
                BottomSheetAction.Primary(
                    text = "단어 추가하기",
                    onClick = onAddWords
                )
            )
        ) {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(generatedWords.size) { index ->
                    val word = generatedWords[index]
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = word.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = word.meaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { onDeleteWord(word) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "삭제",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    } else {
        BottomSheetWithHeader(
            visible = visible,
            onDismiss = onDismiss,
            title = "AI 단어 추가",
            actions = listOf(
                BottomSheetAction.Primary(
                    text = "생성하기",
                    enabled = topic.isNotBlank() && count.isNotBlank(),
                    onClick = onGenerate
                )
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = onTopicChange,
                    label = { Text("주제를 입력해주세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "생성 단어 개수",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                val currentCount = count.toIntOrNull() ?: 0
                                if (currentCount > 1) {
                                    onCountChange((currentCount - 1).toString())
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.minus),
                                contentDescription = "감소",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = count.ifBlank { "0" },
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        
                        OutlinedButton(
                            onClick = {
                                val currentCount = count.toIntOrNull() ?: 0
                                onCountChange((currentCount + 1).toString())
                            },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = "증가",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}




