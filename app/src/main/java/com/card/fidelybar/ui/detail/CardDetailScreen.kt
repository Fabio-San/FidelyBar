package com.card.fidelybar.ui.detail

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardCodeView
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.FavoriteToggle

@Composable
fun CardDetailScreen(
    viewModel: FidelyBarViewModel,
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val card = cards.firstOrNull { it.id == cardId }

    LaunchedEffect(card) {
        if (card == null) onBack()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleLarge,
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
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                CardVisual(
                    card = card,
                    onClick = {},
                    onToggleFavorite = { fav -> viewModel.setFavorite(card.id, fav) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(22.dp))
                CardCodeView(
                    card = card,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22
                )
                Spacer(Modifier.height(20.dp))
                NumberFooter(card)
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
                    viewModel.deleteCard(card.id)
                    onBack()
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
            Text(
                text = card.format.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}