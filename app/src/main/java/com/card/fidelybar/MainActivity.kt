package com.card.fidelybar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.ThemeMode
import com.card.fidelybar.ui.FidelyBarApp
import com.card.fidelybar.ui.components.LogoBitmapCache
import com.card.fidelybar.ui.theme.FidelyBarTheme

class MainActivity : ComponentActivity() {
    private val viewModel: FidelyBarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Lo splash deve seguire il tema impostato: applica la variante scura del
        // tema di avvio quando l'utente ha forzato il DARK (la modalità SYSTEM
        // viene gestita da values-night).
        val darkStarting = AppSettings.themeMode.value == ThemeMode.DARK
        setTheme(
            if (darkStarting) R.style.Theme_FidelyBar_Starting_Dark
            else R.style.Theme_FidelyBar_Starting
        )
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FidelyBarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FidelyBarApp(viewModel)
                }
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= 10) { // ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
            LogoBitmapCache.trimMemory()
        }
    }
}