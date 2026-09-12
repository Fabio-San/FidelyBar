package com.card.fidelybar.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.card.fidelybar.data.UserCard

@Composable
private fun rememberColor(hex: String): Color {
    return remember(hex) {
        val cleaned = hex.removePrefix("#")
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000 or value)
            8 -> Color(value)
            else -> Color(0xFF6750A4)
        }
    }
}

private fun foregroundFor(background: Color): Color {
    val luma = background.luminance()
    val onDark = Color.White
    val onLight = Color(0xFF1B1C1E)
    return if (luma > 0.58f) onLight else onDark
}

@Composable
fun LogoOrMonogram(
    monogram: Char,
    logoUrl: String?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 40,
    logoKey: String? = null
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = monogram.toString(),
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.42f).sp
        )
        val context = LocalContext.current
        val resources = LocalResources.current
        val resId = remember(logoKey) {
            if (logoKey == null) 0
            else resources.getIdentifier("logo_$logoKey", "drawable", context.packageName)
        }
        val cachedFile = remember(logoUrl) {
            logoUrl?.let { com.card.fidelybar.data.LogoCache.fileFor(context, it) }
        }
        val model: Any? = when {
            resId != 0 -> resId
            cachedFile != null -> cachedFile
            logoUrl != null -> logoUrl
            else -> null
        }
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
fun FavoriteToggle(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1f else 0.82f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 620f),
        label = "favoriteScale"
    )
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.12f))
            .clickable(onClick = onToggle)
            .padding(6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CardVisual(
    card: UserCard,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    dense: Boolean = false,
    onClick: () -> Unit
) {
    val primary = rememberColor(card.primaryColorHex)
    val secondary = rememberColor(card.secondaryColorHex)
    val contentColor = foregroundFor(primary)
    val shape = RoundedCornerShape(if (compact) 16.dp else 20.dp)
    val height = when {
        dense -> 98.dp
        compact -> 96.dp
        else -> 196.dp
    }
    val logoSize = when {
        dense -> 46
        compact -> 34
        else -> 42
    }
    val titleStyle = when {
        dense -> MaterialTheme.typography.titleLarge
        compact -> MaterialTheme.typography.titleSmall
        else -> MaterialTheme.typography.titleMedium
    }
    val padding = when {
        dense -> 14.dp
        compact -> 12.dp
        else -> 20.dp
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape),
        color = Color.Transparent,
        shadowElevation = if (compact) 2.dp else 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Brush.linearGradient(listOf(primary, secondary)))
                .clickable(onClick = onClick)
                .padding(padding)
        ) {
            when {
                compact -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LogoOrMonogram(
                            monogram = card.monogram,
                            logoUrl = card.logoUrl,
                            containerColor = contentColor.copy(alpha = 0.16f),
                            contentColor = contentColor,
                            size = logoSize,
                            logoKey = card.presetId
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = card.title,
                                color = contentColor,
                                style = titleStyle,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                softWrap = true,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                dense -> {
                    Box(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.align(Alignment.CenterStart)) {
                            LogoOrMonogram(
                                monogram = card.monogram,
                                logoUrl = card.logoUrl,
                                containerColor = contentColor.copy(alpha = 0.16f),
                                contentColor = contentColor,
                                size = logoSize,
                                logoKey = card.presetId
                            )
                        }
                        Text(
                            text = card.title,
                            color = contentColor,
                            style = titleStyle,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 60.dp)
                        )
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LogoOrMonogram(
                            monogram = card.monogram,
                            logoUrl = card.logoUrl,
                            containerColor = contentColor.copy(alpha = 0.16f),
                            contentColor = contentColor,
                            size = logoSize,
                            logoKey = card.presetId
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = card.title,
                            color = contentColor,
                            style = titleStyle,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}