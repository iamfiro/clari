package com.iamfiro.clari.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iamfiro.clari.core.ui.component.HeaderWithBackButton
import com.iamfiro.clari.core.ui.component.SearchBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamfiro.clari.core.ui.LocalNavBackStack
import com.iamfiro.clari.core.ui.Screen
import com.iamfiro.clari.core.ui.theme.Dimens
import com.iamfiro.clari.feature.wordPack.component.WordPackCard
import com.iamfiro.clari.feature.wordPack.model.dummyWordPacks

@Composable
fun BeforeRecordingScreen() {
    val backStack = LocalNavBackStack.current;
    var query by remember { mutableStateOf("") }

    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { HeaderWithBackButton("녹음을 시작하기 전에\n단어 팩을 선택해주세요") }

                item { Spacer(Modifier.height(2.dp)) }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                    ) {
                        SearchBar(query, { query = it })

                        WordPackCard(
                            dummyWordPacks[0],
                            true
                        )
                        dummyWordPacks.map { pack ->
                            WordPackCard(pack)
                        }
                    }
                }
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
                        backStack.add(Screen.Recording)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("눌러서 시작하기", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}