package com.card.fidelybar.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

enum class FontScale(val key: String, val multiplier: Float) {
    NORMAL("normal", 1.0f),
    LARGE("large", 1.15f),
    EXTRA_LARGE("extra_large", 1.30f);

    companion object {
        fun fromKey(key: String?): FontScale =
            entries.firstOrNull { it.key == key } ?: NORMAL
    }
}

object AppSettings {

    private const val PREFS_NAME = "fidelybar_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_FONT_SCALE = "font_scale"

    private lateinit var prefs: SharedPreferences

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _fontScale = MutableStateFlow(FontScale.NORMAL)
    val fontScale: StateFlow<FontScale> = _fontScale.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeMode.value = ThemeMode.fromKey(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.key))
        _fontScale.value = FontScale.fromKey(prefs.getString(KEY_FONT_SCALE, FontScale.NORMAL.key))
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.key).apply()
        _themeMode.value = mode
    }

    fun setFontScale(scale: FontScale) {
        prefs.edit().putString(KEY_FONT_SCALE, scale.key).apply()
        _fontScale.value = scale
    }
}