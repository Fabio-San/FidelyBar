package com.card.fidelybar.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

internal fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    val value = cleaned.toLong(16)
    return when (cleaned.length) {
        6 -> Color(0xFF000000 or value)
        8 -> Color(value)
        else -> Color(0xFF6750A4)
    }
}

internal fun accentTone(accent: Color, lightness: Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(accent.toArgb(), hsl)
    return Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], hsl[1], lightness.coerceIn(0f, 1f))))
}

internal fun lightenContainer(accent: Color): Color = accentTone(accent, 0.90f)

internal fun darkenContainer(accent: Color): Color = accentTone(accent, 0.30f)

internal fun accentContainerFor(accent: Color, darkTheme: Boolean): Color =
    if (darkTheme) darkenContainer(accent) else lightenContainer(accent)