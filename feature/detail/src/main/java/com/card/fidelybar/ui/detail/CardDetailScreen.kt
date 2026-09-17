package com.card.fidelybar.ui.detail

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardCodeView
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.FavoriteToggle
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush
import kotlinx.coroutines.launch

@Composable
fun CardDetailScreen(
    viewModel: FidelyBarViewModel,
    cardId: String,
    sourceRect: Rect?,
    onClose: () -> Unit,
    onEdit: (String) -> Unit
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val card = cards.firstOrNull { it.id == cardId }

    LaunchedEffect(card) {
        if (card == null) onClose()
    }

    card ?: return

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val activity = view.context as? Activity
        val window = activity?.window
        val original = window?.attributes
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.attributes = original?.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window?.attributes = original?.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    // ---------- Espansione "container transform" ----------
    // La carta parte dalle coordinate della miniatura (sourceRect) e si
    // ingrandisce fino alla sua posizione finale a pieno layout.
    val enterSpec: AnimationSpec<Float> = com.card.fidelybar.ui.WalletSpatial
    val exitSpec: AnimationSpec<Float> = com.card.fidelybar.ui.WalletSpatial
    var targetRect by remember { mutableStateOf<Rect?>(null) }
    val expand = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()
    var exiting by remember { mutableStateOf(false) }

    fun close() {
        if (exiting) return
        exiting = true
        scope.launch {
            expand.animateTo(0f, exitSpec)
            onClose()
        }
    }

    LaunchedEffect(targetRect) {
        if (targetRect != null && expand.value < 1f) {
            expand.animateTo(1f, enterSpec)
        }
    }
    val progress = expand.value
    val endRect = targetRect ?: sourceRect ?: Rect.Zero
    val startRect = sourceRect ?: endRect
    val contentAlpha = ((progress - 0.55f) / 0.45f).coerceIn(0f, 1f)

    BackHandler(enabled = true, onBack = { close() })

    // Contenuto (toolbar, codice, footer): compare dopo che la carta è a ~60%.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FidelyBackgroundBrush())
            .graphicsLayer { alpha = 0.15f + 0.85f * progress }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .graphicsLayer { alpha = contentAlpha },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { close() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                FavoriteToggle(
                    isFavorite = card.isFavorite,
                    onToggle = { viewModel.toggleFavorite(card.id) },
                    tint = if (card.isFavorite) com.card.fidelybar.ui.theme.Gold else MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { onEdit(card.id) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Modifica")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { targetRect = it.boundsInWindow() }
                        .graphicsLayer {
                            if (startRect != endRect && endRect.width > 0f && endRect.height > 0f) {
                                val p = progress
                                val left = startRect.left + (endRect.left - startRect.left) * p
                                val top = startRect.top + (endRect.top - startRect.top) * p
                                val right = startRect.right + (endRect.right - startRect.right) * p
                                val bottom = startRect.bottom + (endRect.bottom - startRect.bottom) * p
                                val current = Rect(left, top, right, bottom)
                                transformOrigin = TransformOrigin(0f, 0f)
                                scaleX = if (endRect.width > 0f) current.width / endRect.width else 1f
                                scaleY = if (endRect.height > 0f) current.height / endRect.height else 1f
                                translationX = current.left - endRect.left
                                translationY = current.top - endRect.top
                            }
                        }
                ) {
                    CardVisual(
                        card = card,
                        onClick = {},
                        dense = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    lerp(28.dp, 20.dp, progress)
                                )
                            )
                    )
                }
                Spacer(Modifier.height(22.dp))
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = contentAlpha
                        translationY = (1f - contentAlpha) * 24f
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CardCodeView(
                        card = card,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22
                    )
                    Spacer(Modifier.height(20.dp))
                    NumberFooter(card)
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina carta") },
            text = { Text("Vuoi eliminare definitivamente «${card.title}»?") },
            confirmButton = {
                TextButton(onClick = {
                    if (exiting) return@TextButton
                    // Avvia l'animazione di uscita e solo al termine elimina la carta:
                    // altrimenti card==null farebbe sparire la schermata all'istante
                    // (catch sul `card ?: return`) saltando la chiusura fluida.
                    showDeleteDialog = false
                    exiting = true
                    scope.launch {
                        expand.animateTo(0f, exitSpec)
                        viewModel.deleteCard(card.id)
                        onClose()
                    }
                }) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun NumberFooter(card: UserCard) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = card.number,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}