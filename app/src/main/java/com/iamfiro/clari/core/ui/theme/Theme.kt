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
    primary = Color(0xFFFFFFFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1C5180),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFF969696),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary = Color(0xFF7E7E7E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF2A2A2A),
    onTertiaryContainer = Color(0xFFE0E0E0),

    error = Color(0xFFFF5959),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0x2DFF5959),
    onErrorContainer = Color(0xFFFF5959),

    background = Color(0xFF121214),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1A1A1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFC4D4E6),

    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF4A4A4A),

    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFFFFFFFF),

    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF000000),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFF7E7E7E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF1A1A1A),

    tertiary = Color(0xFF969696),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0E0E0),
    onTertiaryContainer = Color(0xFF1A1A1A),

    error = Color(0xFFFF5959),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0x1EFF5959),
    onErrorContainer = Color(0xFFCC0000),

    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF494949),

    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFB0B0B0),

    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFFFFFFF),

    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF000000),
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
