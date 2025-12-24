package com.skills.app.core.ui.theme

import Typographys
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF121214),
    onBackground = Color.White,
    surface = Color(0xFF1A1A1E),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0x77E3F1FF),

    secondary = Color(0xFF969696),

    primary = Color(0xFFF8F8F8),
    primaryContainer = Color(0xFF1C5180),
    onPrimary = Color.Black,

    error = Color(0xFFFF5959),
    errorContainer = Color(0x2DFF5959),

    outline = Color(0xFF212121),
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0x9917181A),

    primary = Color(0xFF000000),
    primaryContainer = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    onPrimary = Color.White,

    secondary = Color(0xFF7E7E7E),

    error = Color(0xFFFF5959),
    errorContainer = Color(0x1EFF5959),

    outline = Color(0xFFE3E3E3),
)

@Composable
fun ClariTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typographys,
        content = content
    )
}