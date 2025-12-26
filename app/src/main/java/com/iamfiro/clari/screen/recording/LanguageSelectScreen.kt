package com.iamfiro.clari.screen.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamfiro.clari.core.repository.ExternalResourceRepository
import com.iamfiro.clari.core.repository.KeywordPackRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.component.HeaderWithBackButton
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.recording.Language
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectScreen(projectId: String) {
    val context = LocalContext.current
    val backStack = LocalNavBackStack.current
    val scope = rememberCoroutineScope()
    val keywordPackRepository = remember { KeywordPackRepository.getInstance() }
    val externalResourceRepository = remember { ExternalResourceRepository.getInstance() }
    
    val viewModel: LanguageSelectionViewModel = viewModel(
        factory = LanguageSelectionViewModelFactory(context)
    )

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { HeaderWithBackButton("실시간 음성 인식에 사용할\n언어를 선택해주세요") }

                item { Spacer(Modifier.height(4.dp)) }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                    ) {
                        Language.values().forEach { language ->
                            LanguageOptionCard(
                                language = language,
                                isSelected = selectedLanguage == language,
                                onClick = { viewModel.selectLanguage(language) }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
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
                    onClick = {
                        selectedLanguage?.let { language ->
                            viewModel.saveLanguageSelection()
                            
                            // 프로젝트 상세 정보를 가져와서 externalResourceIds 추출
                            scope.launch {
                                val externalResourceIds = mutableListOf<String>()
                                
                                // 프로젝트 상세 정보 가져오기
                                keywordPackRepository.getKeywordPackById(projectId)
                                    .onSuccess { project ->
                                        // 프로젝트의 connector URL을 기반으로 externalResource 찾기
                                        project.connector?.forEach { connector ->
                                            // 모든 externalResource를 가져와서 URL로 매칭
                                            externalResourceRepository.getAllResources()
                                                .onSuccess { resources ->
                                                    resources.forEach { resource ->
                                                        if (resource.url == connector.url || 
                                                            resource.displayUrl == connector.url) {
                                                            externalResourceIds.add(resource.id)
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                
                                // 모든 비동기 작업 완료 후 Recording 화면으로 이동
                                backStack.add(
                                    Screen.Recording(
                                        projectId = projectId,
                                        languageCode = language.getCountryCode(),
                                        keywordPackIds = listOf(projectId), // 프로젝트 ID를 keywordPackIds에 포함
                                        externalResourceIds = externalResourceIds.distinct() // 중복 제거
                                    )
                                )
                            }
                        }
                    },
                    enabled = selectedLanguage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        if (selectedLanguage != null) "눌러서 시작하기" else "언어를 선택해주세요",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageOptionCard(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = language.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if(isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = language.code,
                style = MaterialTheme.typography.bodyMedium,
                color = if(isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
