package com.iamfiro.clari

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.iamfiro.clari.screen.HomeScreen
import com.iamfiro.clari.screen.NoteListScreen
import com.iamfiro.clari.screen.OnboardScreen
import com.iamfiro.clari.screen.PackListScreen
import com.skills.app.core.ui.theme.ClariTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClariTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    PackListScreen()
                }
            }
        }
    }
}