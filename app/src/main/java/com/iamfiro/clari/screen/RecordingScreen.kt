package com.iamfiro.clari.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iamfiro.clari.feature.note.Component.RecordingControl
import com.iamfiro.clari.feature.note.Component.RecordingHeader
import com.iamfiro.clari.feature.note.Component.TranscribeContainer
import com.iamfiro.clari.feature.note.Component.WordCardOverlay
import com.iamfiro.clari.feature.wordPack.model.dummy_words

@Composable
fun RecordingScreen() {
    Scaffold(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RecordingHeader()
            Box(modifier = Modifier.weight(1f)) {
                TranscribeContainer()
                WordCardOverlay(words = dummy_words)
            }
            RecordingControl()
        }
    }
}