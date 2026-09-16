package com.card.fidelybar.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.foregroundFor
import com.card.fidelybar.ui.components.rememberColor
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush

@Composable
fun HomeScreen(
    viewModel: FidelyBarViewModel,
    onOpenCard: (String, Rect?) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val orderedCards = remember(cards) {
        cards.sortedWith(compareBy { !it.isFavorite })
    }
    var showCredits by remember { mutableStateOf(false) }
    var isListView by rememberSaveable { mutableStateOf(false) }
    val cardBounds = remember { HashMap<String, Rect>() }

    val accentHex by AppSettings.accentHex.collectAsState()
    val accentBase = accentHex?.let { rememberColor(it) }
    val fabColor = accentBase ?: MaterialTheme.colorScheme.primaryContainer
    val fabContentColor = foregroundFor(fabColor)

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
                val fabSource = remember { MutableInteractionSource() }
                val fabPressed by fabSource.collectIsPressedAsState()
                val fabScale by animateFloatAsState(
                    targetValue = if (fabPressed) 0.90f else 1f,
                    animationSpec = spring(
                        dampingRatio = if (fabPressed) Spring.DampingRatioNoBouncy else Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "fabScale"
                )
                val fabContainer by animateColorAsState(
                    targetValue = if (fabPressed)
                        lerp(
                            fabColor,
                            MaterialTheme.colorScheme.primary,
                            0.25f
                        )
                    else fabColor,
                    label = "fabContainer"
                )
                ExtendedFloatingActionButton(
                    onClick = onAddCard,
                    interactionSource = fabSource,
                    containerColor = fabContainer,
                    contentColor = fabContentColor,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nuova carta") },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                    }
                )
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FidelyBackgroundBrush())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                HomeHeader(
                    onCredits = { showCredits = true },
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                )
                if (loadFailed && cards.isEmpty()) {
                    LoadErrorBanner(
                        onRetry = { viewModel.retryLoad() },
                        onDismiss = { viewModel.acknowledgeLoadError() },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Le carte salvate sul dispositivo non sono leggibili. Riprova il recupero oppure autorizza la sostituzione: le nuove carte salvate sovrascriveranno le precedenti.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(Modifier.weight(1f))
                } else if (cards.isEmpty()) {
                    EmptyHome(
                        onAdd = onAddCard,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 110.dp)
                    ) {

                    item {
                        SectionHeader(
                            title = "Le tue carte",
                            isListView = isListView,
                            onToggleView = { isListView = !isListView },
                            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 4.dp)
                        )
                    }

                    items(orderedCards.chunked(2), key = { it.joinToString("") { c -> c.id } }) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { card ->
                                Box(modifier = Modifier.weight(1f)) {
                                    AnimatedContent(
                                        targetState = isListView,
                                        transitionSpec = {
                                            (fadeIn(tween(300)) +
                                                scaleIn(initialScale = 0.94f, animationSpec = tween(300)))
                                                .togetherWith(
                                                    fadeOut(tween(150)) +
                                                        scaleOut(targetScale = 0.97f, animationSpec = tween(150))
                                                )
                                        },
                                        label = "viewMorph"
                                    ) { list ->
                                        if (list) {
                                            CardListItem(
                                                card = card,
                                                modifier = Modifier.fillMaxWidth(),
                                                onBounds = { rect -> cardBounds[card.id] = rect },
                                                onClick = { onOpenCard(card.id, cardBounds[card.id]) }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(96.dp)
                                            ) {
                                                CardVisual(
                                                    card = card,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .onGloballyPositioned { cardBounds[card.id] = it.boundsInWindow() },
                                                    compact = true,
                                                    onClick = { onOpenCard(card.id, cardBounds[card.id]) }
                                                )
                                            }
                                        }
                                    }
                                }
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onCredits) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Crediti",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Impostazioni",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Le tue carte, sempre con te",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onBounds: (Rect) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier.onGloballyPositioned { onBounds(it.boundsInWindow()) }
    ) {
        val primary = rememberColor(card.primaryColorHex)
        val secondary = rememberColor(card.secondaryColorHex)
        val contentColor = foregroundFor(primary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(primary, secondary)))
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadErrorBanner(
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Impossibile leggere le carte salvate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onRetry) { Text("Riprova") }
                TextButton(onClick = onDismiss) { Text("Sostituisci") }
            }
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
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
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
        val ctaSource = remember { MutableInteractionSource() }
        val ctaPressed by ctaSource.collectIsPressedAsState()
        val ctaScale by animateFloatAsState(
            targetValue = if (ctaPressed) 0.96f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "ctaScale"
        )
        Button(
            onClick = onAdd,
            interactionSource = ctaSource,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.graphicsLayer {
                scaleX = ctaScale
                scaleY = ctaScale
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Aggiungi la prima carta")
        }
    }
}