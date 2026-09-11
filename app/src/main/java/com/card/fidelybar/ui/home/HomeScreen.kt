package com.card.fidelybar.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.theme.Gold
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: FidelyBarViewModel,
    onOpenCard: (String) -> Unit,
    onAddCard: () -> Unit
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val favorites = cards.filter { it.isFavorite }
    val others = cards.filterNot { it.isFavorite }

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
                        HomeHeader(total = cards.size, modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp))
                    }

                    if (favorites.isNotEmpty()) {
                        item {
                            FavoritesPager(
                                cards = favorites,
                                onOpen = onOpenCard,
                                onToggleFavorite = viewModel::toggleFavorite
                            )
                        }
                        item {
                            DotsIndicator(
                                pageCount = favorites.size,
                                modifier = Modifier.padding(top = 14.dp)
                            )
                        }

                        if (others.isNotEmpty()) {
                            item {
                                SectionTitle(
                                    "Tutte le carte",
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 10.dp)
                                )
                            }
                        }
                    } else {
                        item {
                            SectionTitle(
                                "Le tue carte",
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp)
                            )
                        }
                    }

                    items(others.chunked(2), key = { it.joinToString("") { c -> c.id } }) { row ->
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
                                    onClick = { onOpenCard(card.id) },
                                    onToggleFavorite = { fav -> viewModel.setFavorite(card.id, fav) }
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

@Composable
private fun HomeHeader(total: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "FidelyBar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$total",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
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
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun FavoritesPager(
    cards: List<UserCard>,
    onOpen: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { cards.size })
    val pageWidthFraction = 0.82f
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val pageWidth = (screenWidthDp * pageWidthFraction).dp

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(pageWidth),
        contentPadding = PaddingValues(horizontal = 30.dp),
        pageSpacing = 14.dp,
        beyondViewportPageCount = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) { page ->
        val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val scale = (1f - 0.10f * abs(offset)).coerceIn(0.82f, 1f)
        CardVisual(
            card = cards[page],
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            onClick = { onOpen(cards[page].id) },
            onToggleFavorite = { onToggleFavorite(cards[page].id) }
        )
    }
}

@Composable
private fun DotsIndicator(pageCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount.coerceAtMost(9)) { index ->
            val active = index == 0
            val color = if (active) Gold else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = if (active) 18.dp else 7.dp, height = 7.dp)
                    .clip(CircleShape)
                    .background(color)
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