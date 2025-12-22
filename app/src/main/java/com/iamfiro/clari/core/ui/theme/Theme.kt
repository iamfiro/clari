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

    primary = Color(0xFF2289FF),
    primaryContainer = Color(0x802289FF),
    onPrimary = Color.White,

    error = Color(0xFFFF5959),
    errorContainer = Color(0x80FF5959),

    outline = Color(0xFF212121),
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0x9917181A),

    primary = Color(0xFF2289FF),
    primaryContainer = Color(0x802289FF),
    onPrimary = Color.White,

    secondary = Color(0xFF969696),

    error = Color(0xFFFF5959),
    errorContainer = Color(0x80FF5959),

    outline = Color(0xFFE9E9E9),
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