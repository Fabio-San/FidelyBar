package com.card.fidelybar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.LogoOrMonogram

@Composable
fun HomeScreen(
    viewModel: FidelyBarViewModel,
    onOpenCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val orderedCards = remember(cards) {
        cards.sortedWith(compareBy { !it.isFavorite })
    }
    var showCredits by remember { mutableStateOf(false) }
    var isListView by rememberSaveable { mutableStateOf(false) }

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            title = { Text("Crediti") },
            text = {
                Column {
                    Text("Loghi e dati delle catene: Wikipedia e Wikimedia Commons.")
                    Spacer(Modifier.height(10.dp))
                    Text("Concessi in licenza Creative Commons Attribution-ShareAlike 4.0 (CC BY-SA 4.0).")
                    Spacer(Modifier.height(10.dp))
                    Text("FidelyBar è un progetto personale. I marchi appartengono ai rispettivi detentori.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCredits = false }) { Text("Chiudi") }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (cards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddCard,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nuova carta") },
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
    ) { inner ->
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
            if (cards.isEmpty()) {
                EmptyHome(
                    onAdd = onAddCard,
                    modifier = Modifier.padding(inner)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    item {
                        HomeHeader(
                            onCredits = { showCredits = true },
                            onOpenSettings = onOpenSettings,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                        )
                    }

                    item {
                        SectionHeader(
                            title = "Le tue carte",
                            isListView = isListView,
                            onToggleView = { isListView = !isListView },
                            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 4.dp)
                        )
                    }

                    if (isListView) {
                        items(orderedCards.chunked(2), key = { it.joinToString("") { c -> c.id } }) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { card ->
                                    CardListItem(
                                        card = card,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenCard(card.id) }
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        items(orderedCards.chunked(2), key = { it.joinToString("") { c -> c.id } }) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { card ->
                                    CardVisual(
                                        card = card,
                                        modifier = Modifier.weight(1f),
                                        compact = true,
                                        onClick = { onOpenCard(card.id) }
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onCredits: () -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "FidelyBar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onCredits) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Crediti",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Impostazioni",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Le tue carte, sempre con te",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    isListView: Boolean,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleView, modifier = Modifier.size(38.dp)) {
            Icon(
                imageVector = if (isListView) Icons.Filled.GridView else Icons.AutoMirrored.Filled.List,
                contentDescription = if (isListView) "Passa alla griglia" else "Passa all'elenco",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CardListItem(
    card: UserCard,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = remember(card.primaryColorHex) {
        Color(card.primaryColorHex.toColorInt())
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LogoOrMonogram(
                monogram = card.monogram,
                logoUrl = card.logoUrl,
                containerColor = accent,
                contentColor = Color.White,
                size = 40,
                logoKey = card.presetId
            )
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyHome(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Ancora nessuna carta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Aggiungi le tue carte fedeltà per averle sempre con te, anche offline. Un tap e sei alla cassa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Aggiungi la prima carta")
        }
    }
}