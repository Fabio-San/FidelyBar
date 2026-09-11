package com.card.fidelybar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.card.fidelybar.ui.FidelyBarApp
import com.card.fidelybar.ui.theme.FidelyBarTheme

class MainActivity : ComponentActivity() {
    private val viewModel: FidelyBarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FidelyBarTheme {
                androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
                    FidelyBarApp(viewModel)
                }
            }
        }
    }
}