package com.card.fidelybar.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.FontScale
import com.card.fidelybar.data.ThemeMode

val LocalDarkTheme = staticCompositionLocalOf { false }

internal val LocalStatusBarTint = staticCompositionLocalOf { false }
internal val LocalAccentContainer = staticCompositionLocalOf { Color.Unspecified }

@Composable
fun FidelyBackgroundBrush(): Brush {
    val tint = LocalStatusBarTint.current
    val container = LocalAccentContainer.current
    val bg = MaterialTheme.colorScheme.background
    return if (tint && container != Color.Unspecified && container != bg) {
        Brush.verticalGradient(listOf(container, bg))
    } else {
        SolidColor(bg)
    }
}

private fun ColorScheme.withAccent(accent: Color, dark: Boolean): ColorScheme {
    return if (dark) {
        copy(
            primary = accentTone(accent, 0.80f),
            onPrimary = accentTone(accent, 0.20f),
            primaryContainer = accentTone(accent, 0.30f),
            onPrimaryContainer = accentTone(accent, 0.90f)
        )
    } else {
        copy(
            primary = accent,
            onPrimary = if (accent.luminance() > 0.5f) Ink else Color.White,
            primaryContainer = accentTone(accent, 0.90f),
            onPrimaryContainer = accentTone(accent, 0.15f)
        )
    }
}

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
    background = Color(0xFF111418),
    onBackground = Color(0xFFE2E3E5),
    surface = Color(0xFF111418),
    onSurface = Color(0xFFE2E3E5),
    surfaceVariant = Color(0xFF2A2D31),
    onSurfaceVariant = Color(0xFFC6C9CE),
    outline = Color(0xFF8E9198)
)

private val LightColorScheme = lightColorScheme(
    primary = FidiGreen40,
    onPrimary = Color.White,
    primaryContainer = FidiGreen80,
    onPrimaryContainer = Color(0xFF0E3326),
    secondary = Clay40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8C5A4),
    onSecondaryContainer = Color(0xFF3B1C06),
    tertiary = Sand40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE29A),
    onTertiaryContainer = Color(0xFF2C1E00),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFBF9F4),
    onBackground = Ink,
    surface = Color(0xFFFDFCF8),
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDEAE2),
    onSurfaceVariant = Color(0xFF57574F),
    outline = Color(0xFF8A8A82),
    outlineVariant = Color(0xFFDBD8D0)
)

@Composable
fun FidelyBarTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeMode by AppSettings.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val fontScale by AppSettings.fontScale.collectAsState()
    val typography = Typography.scaled(fontScale.multiplier)

    val accentHex by AppSettings.accentHex.collectAsState()
    val statusBarTint by AppSettings.statusBarTint.collectAsState()

    val colorScheme = (if (darkTheme) DarkColorScheme else LightColorScheme)
        .let { base ->
            accentHex?.let { base.withAccent(parseHexColor(it), darkTheme) } ?: base
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
                @Suppress("DEPRECATION")
                window.navigationBarColor = AndroidColor.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    window.isNavigationBarContrastEnforced = false
                }
            }
        }
    }

    val accentContainer = accentHex?.let { accentContainerFor(parseHexColor(it), darkTheme) }
        ?: colorScheme.primaryContainer

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalStatusBarTint provides statusBarTint,
        LocalAccentContainer provides accentContainer
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = FidelyShapes,
            content = content
        )
    }
}