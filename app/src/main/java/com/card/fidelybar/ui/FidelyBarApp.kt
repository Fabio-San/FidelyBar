package com.card.fidelybar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.ui.components.FullScreenHost
import com.card.fidelybar.ui.detail.CardDetailScreen
import com.card.fidelybar.ui.editor.EditorScreen
import com.card.fidelybar.ui.home.HomeScreen
import com.card.fidelybar.ui.settings.SettingsScreen
import com.card.fidelybar.ui.onboarding.OnboardingDialog
import com.card.fidelybar.data.AppSettings
import androidx.compose.runtime.collectAsState
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class EditorRequest(val cardId: String?)

private data class DetailRequest(val cardId: String, val sourceRect: Rect?)

@Composable
fun FidelyBarApp(viewModel: FidelyBarViewModel) {
    var editor by remember { mutableStateOf<EditorRequest?>(null) }
    var editorOpenId by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<DetailRequest?>(null) }
    var forceShowOnboarding by remember { mutableStateOf(false) }

    val hasSeenOnboarding by AppSettings.hasSeenOnboarding.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var booted by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (booted) return@LaunchedEffect
        if (!isLoading) booted = true
    }

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

    if (booted) {
        Box(modifier = Modifier.fillMaxSize()) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenCard = { id, source -> detail = DetailRequest(id, source) },
                    onAddCard = {
                        editorOpenId++
                        editor = EditorRequest(null)
                    },
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
                            editorOpenId++
                            editor = EditorRequest(id)
                        }
                    )
                }

                // Editor e Impostazioni sono schermate a schermo intero (overlay),
                // sempre composte anche da chiuse: pre-riscaldano e l'uscita è animata
                // con la stessa molla dell'ingresso.
                FullScreenHost(visible = editor != null, onDismiss = { dismissWithImeHandoff { editor = null } }) {
                    // key(): a ogni apertura (contatore incrementato) lo stato rememberSaveable
                    // dell'editor viene smontato e ricreato, evitando di riproporre i dati della
                    // card precedente quando si tocca "Nuova carta" dopo averne completata una.
                    key(editorOpenId) {
                        EditorScreen(
                            viewModel = viewModel,
                            cardId = editor?.cardId,
                            inSheet = false,
                            active = editor != null,
                            onBack = { dismissWithImeHandoff { editor = null } },
                            onSaved = { dismissWithImeHandoff { editor = null } }
                        )
                    }
                }

                FullScreenHost(visible = showSettings, onDismiss = { showSettings = false }) {
                    SettingsScreen(
                        viewModel = viewModel,
                        inSheet = false,
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
        } else {
            StartupPlaceholder()
        }
}

@Composable
private fun StartupPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FidelyBackgroundBrush()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "FidelyBar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Le tue carte, sempre con te",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}