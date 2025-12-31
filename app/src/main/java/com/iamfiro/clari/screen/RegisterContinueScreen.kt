package com.iamfiro.clari.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.network.ApiClient
import com.iamfiro.clari.core.repository.AuthRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.theme.Dimens
import kotlinx.coroutines.launch

private const val TAG = "RegisterContinueScreen"

private val roleOptions = listOf(
    "개발자", "디자이너", "마케터", "기획자", "데이터 분석가",
    "프로덕트 매니저", "학생", "선생님", "교수", "연구원",
    "회계사", "변호사", "의사", "간호사", "약사",
    "영업", "인사", "경영", "컨설턴트", "프리랜서"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterContinueScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current
    val scrollState = rememberScrollState()

    var currentStep by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isCustomInputMode by remember { mutableStateOf(false) }
    var customRoleText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    val nameFocusRequester = remember { FocusRequester() }

    val authRepository = remember {
        AuthRepository.getInstance(ApiClient.getTokenManager())
    }
    
    LaunchedEffect(isCustomInputMode) {
        if (isCustomInputMode) {
            focusRequester.requestFocus()
        }
    }
    
    LaunchedEffect(currentStep) {
        if (currentStep == 0) {
            nameFocusRequester.requestFocus()
        }
    }

    fun handleRegisterContinue() {
        val role = if (isCustomInputMode) {
            customRoleText.trim().ifEmpty { null }
        } else {
            selectedRole
        } ?: return
        
        val name = userName.trim().ifEmpty { null }

        if (isLoading) return

        coroutineScope.launch {
            Log.d(TAG, "회원가입 완료 요청 - role: $role, name: $name")
            isLoading = true

            authRepository.registerContinue(role = role, name = name)
                .onSuccess { user ->
                    Log.d(TAG, "회원가입 완료 성공 - isActive: ${user.isActive}")
                    backStack.clear()
                    backStack.add(Screen.Home)
                }
                .onFailure { error ->
                    Log.e(TAG, "회원가입 완료 실패", error)
                    Toast.makeText(
                        context,
                        "회원가입 완료에 실패했습니다: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            isLoading = false
        }
    }
    
    fun goToNextStep() {
        if (currentStep == 0 && userName.trim().isNotEmpty()) {
            currentStep = 1
        }
    }
    
    val isNextEnabled = when (currentStep) {
        0 -> userName.trim().isNotEmpty()
        1 -> if (isCustomInputMode) customRoleText.trim().isNotEmpty() else selectedRole != null
        else -> false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(top = 60.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                when (currentStep) {
                    0 -> NameInputStep(
                        userName = userName,
                        onUserNameChange = { userName = it },
                        focusRequester = nameFocusRequester
                    )
                    1 -> RoleSelectionStep(
                        selectedRole = selectedRole,
                        isCustomInputMode = isCustomInputMode,
                        customRoleText = customRoleText,
                        onRoleSelect = { role ->
                            isCustomInputMode = false
                            selectedRole = role
                        },
                        onCustomInputClick = {
                            isCustomInputMode = true
                            selectedRole = null
                        },
                        onCustomRoleTextChange = { customRoleText = it },
                        focusRequester = focusRequester
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = {
                    if (currentStep == 0) {
                        goToNextStep()
                    } else {
                        handleRegisterContinue()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = isNextEnabled && !isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        if (currentStep == 0) "다음" else "시작하기",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NameInputStep(
    userName: String,
    onUserNameChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Text(
        text = "이름을 알려주세요",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "Clari에서 사용할 이름이에요",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    OutlinedTextField(
        value = userName,
        onValueChange = onUserNameChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = {
            Text(
                "이름을 입력해주세요",
                color = MaterialTheme.colorScheme.secondary
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleSelectionStep(
    selectedRole: String?,
    isCustomInputMode: Boolean,
    customRoleText: String,
    onRoleSelect: (String) -> Unit,
    onCustomInputClick: () -> Unit,
    onCustomRoleTextChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Text(
        text = "어떤 일을 하시나요?",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "맞춤화된 AI 설명을 제공해드려요",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        roleOptions.forEach { role ->
            RoleChip(
                text = role,
                isSelected = !isCustomInputMode && selectedRole == role,
                onClick = { onRoleSelect(role) }
            )
        }
        
        if (isCustomInputMode) {
            CustomInputChip(
                value = customRoleText,
                onValueChange = onCustomRoleTextChange,
                focusRequester = focusRequester
            )
        } else {
            RoleChip(
                text = "직접 입력",
                isSelected = false,
                onClick = onCustomInputClick
            )
        }
    }
}

@Composable
private fun RoleChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
private fun CustomInputChip(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "직접 입력해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
