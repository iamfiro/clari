package com.iamfiro.clari.feature.project.component

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.component.BottomSheetAction
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
    onGetAiSuggestions: ((String) -> Unit)? = null
) {
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
        
        // AI 제안 표시
        if (aiSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AI 추천 설명",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                aiSuggestions.forEach { suggestion ->
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { onWordMeaningChange(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = wordMeaning,
            onValueChange = onWordMeaningChange,
            label = { Text("단어 뜻") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )
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
    onAdd: () -> Unit
) {
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "외부 연결 추가",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "추가",
                enabled = selectedConnectorType != null && connectorName.isNotBlank() && connectorUrl.isNotBlank(),
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
    onCopyLink: () -> Unit
) {
    BottomSheetWithHeader(
        visible = visible,
        onDismiss = onDismiss,
        title = "프로젝트 공유",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "링크 복사",
                onClick = onCopyLink
            )
        )
    ) {
        OutlinedTextField(
            value = shareLink,
            onValueChange = {},
            label = { Text("공유 링크") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true
        )
    }
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




