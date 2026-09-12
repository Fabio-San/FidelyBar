package com.card.fidelybar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = FidiGreen80,
    onPrimary = Color(0xFF003921),
    primaryContainer = Color(0xFF005235),
    onPrimaryContainer = Color(0xFFB7F2D2),
    secondary = Clay80,
    onSecondary = Color(0xFF442A1C),
    secondaryContainer = Color(0xFF5D3F2E),
    onSecondaryContainer = Color(0xFFFFDBB8),
    tertiary = Sand80,
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5A4300),
    onTertiaryContainer = Color(0xFFFFE28B),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF11140F),
    onBackground = Color(0xFFE1E4DC),
    surface = Color(0xFF11140F),
    onSurface = Color(0xFFE1E4DC),
    surfaceVariant = Color(0xFF253227),
    onSurfaceVariant = Color(0xFF4F6B58),
    outline = Color(0xFF9BB6A4)
)

private val LightColorScheme = lightColorScheme(
    primary = FidiGreen40,
    onPrimary = Color.White,
    primaryContainer = FidiGreen80,
    onPrimaryContainer = Color(0xFF002113),
    secondary = Clay40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8C5A4),
    onSecondaryContainer = Color(0xFF3B1C06),
    tertiary = Sand40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE29A),
    onTertiaryContainer = Color(0xFF2C1E00),
    error = Color(0xFFBA1A1A),
    background = Cream,
    onBackground = Ink,
    surface = Color(0xFFFFF9F0),
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF0E8),
    onSurfaceVariant = Slate,
    outline = Color(0xFFB0B6A8)
)

@Composable
fun FidelyBarTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeMode by AppSettings.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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
        typography = Typography,
        shapes = FidelyShapes,
        content = content
    )
}