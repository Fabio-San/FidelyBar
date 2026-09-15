package com.card.fidelybar.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample

/**
 * Osserva la visibilità della tastiera (IME) in modo NON invasivo, ovvero senza
 * leggerla in composizione (che sottoscriverebbe gli insets e ricomporrebbe a ogni
 * fotogramma dell'animazione). La lettura avviene in una coroutine [snapshotFlow]
 * a lato della composizione: lo stato cambia solo quando la tastiera termina
 * l'animazione, non durante.
 */
@Composable
fun rememberImeVisible(): State<Boolean> {
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        imeVisibilityFlow(ime, density).collect { isVisible ->
            if (visible.value != isVisible) visible.value = isVisible
        }
    }
    return visible
}

/** 1 → tastiera aperta, 0 → chiusa. Campionato per ignorare i fotogrammi intermedi. */
private fun imeVisibilityFlow(
    ime: WindowInsets,
    density: androidx.compose.ui.unit.Density
): Flow<Boolean> =
    snapshotFlow { ime.getBottom(density) }
        .sample(keyboardSampleMs)
        .map { it > 0 }
        .distinctUntilChanged()

private const val keyboardSampleMs = 80L