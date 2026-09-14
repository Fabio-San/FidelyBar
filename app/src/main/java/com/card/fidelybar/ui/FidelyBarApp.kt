package com.card.fidelybar.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.ui.components.FidelySheet
import com.card.fidelybar.ui.detail.CardDetailScreen
import com.card.fidelybar.ui.editor.EditorScreen
import com.card.fidelybar.ui.home.HomeScreen
import com.card.fidelybar.ui.settings.SettingsScreen
import com.card.fidelybar.ui.onboarding.OnboardingDialog
import com.card.fidelybar.data.AppSettings
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Molle "Espressive" (stile Material 3 / Google Wallet):
// - spatial: un leggero overshoot che si assesta morbido
// - effects: niente rimbalzo, solo energia (fade/colore)
val WalletSpatial: AnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 620f)
val WalletEffects: AnimationSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 900f)

private data class EditorRequest(val cardId: String?)

private data class DetailRequest(val cardId: String, val sourceRect: Rect?)

@Composable
fun FidelyBarApp(viewModel: FidelyBarViewModel) {
    var editor by remember { mutableStateOf<EditorRequest?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<DetailRequest?>(null) }
    var forceShowOnboarding by remember { mutableStateOf(false) }

    val hasSeenOnboarding by AppSettings.hasSeenOnboarding.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Tastiera letta al momento della chiamata (imperativo via ViewCompat), NON in
    // composizione: leggere WindowInsets.ime in composizione sotto-scrive agli insets
    // e ricomporrebbe l'intera app a OGNI fotogramma dell'animazione della tastiera
    // (le sheet restano sempre composte) -> salita a scatti.
    fun isImeVisible(): Boolean {
        val root = ViewCompat.getRootWindowInsets(view) ?: return false
        return root.isVisible(WindowInsetsCompat.Type.ime())
    }

    // Se la tastiera è aperta, la sheet sta "appoggiata" sopra: chiuderla subito
    // farebbe scivolare giù solo la parte visibile (quasi niente) -> sembra istantanea.
    // Prima retrai la tastiera, attendo che si chiuda, e solo poi animo la chiusura
    // della sheet completa: la molla di uscita resta identica a quella di ingresso.
    fun dismissWithImeHandoff(dismiss: () -> Unit) {
        if (isImeVisible()) {
            keyboard?.hide()
            scope.launch {
                delay(220)
                dismiss()
            }
        } else {
            dismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreen(
            viewModel = viewModel,
            onOpenCard = { id, source -> detail = DetailRequest(id, source) },
            onAddCard = { editor = EditorRequest(null) },
            onOpenSettings = { showSettings = true }
        )

        detail?.let { req ->
            CardDetailScreen(
                viewModel = viewModel,
                cardId = req.cardId,
                sourceRect = req.sourceRect,
                onClose = { detail = null },
                onEdit = { id ->
                    detail = null
                    editor = EditorRequest(id)
                }
            )
        }

        // FidelySheet sempre composta (anche da chiusa): pre-riscalda Editor e
        // Impostazioni e, all'uscita, anima via con la stessa molla dell'ingresso.
        FidelySheet(visible = editor != null, onDismiss = { dismissWithImeHandoff { editor = null } }) {
            EditorScreen(
                viewModel = viewModel,
                cardId = editor?.cardId,
                inSheet = true,
                onBack = { dismissWithImeHandoff { editor = null } },
                onSaved = { dismissWithImeHandoff { editor = null } }
            )
        }

        FidelySheet(visible = showSettings, onDismiss = { showSettings = false }) {
            SettingsScreen(
                viewModel = viewModel,
                inSheet = true,
                onBack = { showSettings = false },
                onShowTutorial = {
                    showSettings = false
                    forceShowOnboarding = true
                }
            )
        }

        if (!hasSeenOnboarding || forceShowOnboarding) {
            OnboardingDialog(
                onFinished = {
                    forceShowOnboarding = false
                }
            )
        }
    }
}