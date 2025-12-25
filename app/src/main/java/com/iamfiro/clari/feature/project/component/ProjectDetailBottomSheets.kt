package com.iamfiro.clari.feature.project.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.R
import com.iamfiro.clari.core.ui.component.BottomSheetAction
import com.iamfiro.clari.core.ui.component.BottomSheetWithHeader
import com.iamfiro.clari.core.ui.component.ConfirmBottomSheet
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType
import com.iamfiro.clari.screen.project.ProjectDetailViewModel

@Composable
fun AddWordBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    wordName: String,
    wordMeaning: String,
    onWordNameChange: (String) -> Unit,
    onWordMeaningChange: (String) -> Unit,
    onAdd: () -> Unit
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
            singleLine = true
        )
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




