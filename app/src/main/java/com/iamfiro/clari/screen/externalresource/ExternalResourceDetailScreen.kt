package com.iamfiro.clari.screen.externalresource

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iamfiro.clari.R
import com.iamfiro.clari.core.repository.ExternalResourceRepository
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.component.BottomSheetAction
import com.iamfiro.clari.core.ui.component.BottomSheetWithHeader
import com.iamfiro.clari.core.ui.component.SectionTitle
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.externalresource.model.ExternalResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ExternalResourceDetailScreen(resourceId: String) {
    val repository = remember { ExternalResourceRepository.getInstance() }
    val backStack = LocalNavBackStack.current
    val uriHandler = LocalUriHandler.current
    
    var resource by remember { mutableStateOf<ExternalResource?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var showEditSheet by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf("") }

    LaunchedEffect(resourceId) {
        isLoading = true
        repository.getResourceById(resourceId)
            .onSuccess { res ->
                resource = res
                editTitle = res.title
            }
            .onFailure { e ->
                error = e.message
            }
        isLoading = false
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 헤더
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = "back",
                    modifier = Modifier.clickable { backStack.removeLastOrNull() }
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "편집")
                    }
                    IconButton(onClick = { 
                        resource?.url?.let { uriHandler.openUri(it) }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "열기")
                    }
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            error ?: "알 수 없는 오류",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                resource != null -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Dimens.ScreenPadding)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            if (resource!!.logoUrl != null) {
                                AsyncImage(
                                    model = resource!!.logoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        resource!!.title.firstOrNull()?.toString() ?: "?",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Column {
                                Text(
                                    resource!!.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    resource!!.displayUrl,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        if (!resource!!.scrapedContent.isNullOrBlank()) {
                            SectionTitle("스크랩된 내용")
                            Spacer(Modifier.height(12.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    resource!!.scrapedContent!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "스크랩된 내용이 없습니다",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    BottomSheetWithHeader(
        visible = showEditSheet,
        onDismiss = {
            showEditSheet = false
            editTitle = resource?.title ?: ""
        },
        title = "제목 수정",
        actions = listOf(
            BottomSheetAction.Primary(
                text = "저장",
                enabled = editTitle.isNotBlank() && editTitle.length <= 10,
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        repository.updateResourceTitle(resourceId, editTitle)
                            .onSuccess { updated ->
                                resource = updated
                                showEditSheet = false
                            }
                    }
                }
            )
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = editTitle,
                onValueChange = { if (it.length <= 10) editTitle = it },
                label = { Text("제목 (최대 10자)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("${editTitle.length}/10") }
            )
        }
    }
}
