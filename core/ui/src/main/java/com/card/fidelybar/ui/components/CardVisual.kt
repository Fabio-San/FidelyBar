package com.card.fidelybar.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.card.fidelybar.data.UserCard
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

@Composable
fun rememberColor(hex: String): Color {
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

fun foregroundFor(background: Color): Color {
    val luma = background.luminance()
    val onDark = Color.White
    val onLight = Color(0xFF1B1C1E)
    return if (luma > 0.58f) onLight else onDark
}

object LogoBitmapCache {
    const val DEFAULT_MAX_PX = 2048

    // Cache loghi: le bitmap vivono nella memoria nativa (fuori heap), quindi è
// bene tenerla contenuta e svuotarla quando il sistema segnala scarsità di RAM.
private val cache = object : LruCache<String, ImageBitmap>(48 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
    }

    fun trimMemory() {
        cache.evictAll()
    }

    fun clear() {
        cache.evictAll()
    }

    val decodeDispatcher: CoroutineDispatcher =
        Executors.newFixedThreadPool(2) { r ->
            Thread(r, "logo-decode").apply { priority = Thread.NORM_PRIORITY }
        }.asCoroutineDispatcher()

    @get:Suppress("DiscouragedApi")
    private val resIds = HashMap<String, Int>()

    @Synchronized
    @Suppress("DiscouragedApi")
    fun resId(context: Context, logoKey: String): Int = resIds.getOrPut(logoKey) {
        context.resources.getIdentifier("logo_$logoKey", "drawable", context.packageName)
    }

    private fun bucket(maxPx: Int): Int {
        var b = 1
        while (b < maxPx) b = b shl 1
        return b
    }

    @Synchronized
    fun get(context: Context, logoKey: String, maxPx: Int = DEFAULT_MAX_PX): ImageBitmap? {
        val target = bucket(maxPx)
        val cacheKey = "$logoKey@$target"
        cache.get(cacheKey)?.let { return it }
        val resId = resId(context, logoKey)
        if (resId == 0) return null
        val bitmap = decode(context, resId, target) ?: return null
        val image = bitmap.asImageBitmap()
        cache.put(cacheKey, image)
        return image
    }

    fun getCached(context: Context, logoKey: String?, maxPx: Int): ImageBitmap? {
        if (logoKey == null) return null
        return cache.get("$logoKey@${bucket(maxPx)}")
    }

    private fun decode(context: Context, resId: Int, maxPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxPx || bounds.outHeight / sample > maxPx) {
            sample *= 2
        }
        return BitmapFactory.decodeResource(
            context.resources, resId,
            BitmapFactory.Options().apply { inSampleSize = sample }
        )
    }

    fun warmAll(context: Context) {
        val density = context.resources.displayMetrics.density
        val gridPx = bucket((150f * density * 2f).roundToInt()).coerceAtMost(DEFAULT_MAX_PX)
        Thread(
            {
                com.card.fidelybar.data.StoreCatalog.all.forEach { preset ->
                    get(context, preset.id, 256)
                    get(context, preset.id, gridPx)
                }
            },
            "logo-warmup"
        ).start()
    }
}

@Composable
fun LogoOrMonogram(
    monogram: Char,
    logoUrl: String?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 40,
    logoKey: String? = null,
    circle: Boolean = true,
    borderWhite: Boolean = false,
    borderSize: Int = 1,
    decodeMaxPx: Int = 0
) {
    val borderDp = borderSize.coerceIn(1, 8).dp
    val boxModifier = if (circle) {
        modifier
            .size(size.dp)
            .then(
                if (borderWhite) Modifier
                    .background(Color.White, CircleShape)
                    .padding(borderDp)
                else Modifier
            )
            .clip(CircleShape)
            .background(containerColor)
    } else {
        modifier
    }
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current.density
        val targetPx = remember(logoKey, decodeMaxPx, size, density) {
            if (decodeMaxPx > 0) decodeMaxPx
            else (size * density * 2f).roundToInt().coerceIn(64, LogoBitmapCache.DEFAULT_MAX_PX)
        }
        var localLogo by remember(logoKey, targetPx) {
            mutableStateOf(LogoBitmapCache.getCached(context, logoKey, targetPx))
        }
        LaunchedEffect(logoKey, targetPx) {
            if (localLogo == null) {
                val decoded = withContext(LogoBitmapCache.decodeDispatcher) {
                    if (logoKey == null) null else LogoBitmapCache.get(context, logoKey, targetPx)
                }
                if (decoded != null) localLogo = decoded
            }
        }
        val cachedFile = remember(logoUrl) {
            logoUrl?.let { com.card.fidelybar.data.LogoCache.fileFor(context, it) }
        }
        val currentLogo = localLogo
        if (borderWhite && !circle && currentLogo != null) {
            val s = borderSize.coerceIn(1, 8)
            listOf(
                -s to 0,
                s to 0,
                0 to -s,
                0 to s
            ).forEach { (dx, dy) ->
                Image(
                    bitmap = currentLogo,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        Color.White,
                        androidx.compose.ui.graphics.BlendMode.SrcIn
                    ),
                    modifier = Modifier
                        .matchParentSize()
                        .offset(dx.dp, dy.dp)
                )
            }
        }
        val model: Any? = when {
            currentLogo != null -> null
            cachedFile != null -> cachedFile
            logoUrl != null -> logoUrl
            else -> null
        }
        val crossfadeState = if (currentLogo != null) 1 else if (model != null) 2 else 0
        val logoModifier = Modifier
            .matchParentSize()
            .then(
                if (circle) Modifier.padding(if (borderWhite) 3.dp else 5.dp)
                else Modifier
            )
        Crossfade(
            targetState = crossfadeState,
            animationSpec = tween(durationMillis = 120),
            label = "logoCrossfade",
            modifier = logoModifier
        ) { target ->
            when (target) {
                1 -> Image(
                    bitmap = currentLogo!!,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> AsyncImage(
                    model = model!!,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                else -> Text(
                    text = monogram.toString(),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size * 0.42f).sp
                )
            }
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
    elevation: Dp? = null,
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
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val hasLogo = remember(card.presetId, card.logoUrl) {
        val pid = card.presetId
        card.logoUrl != null || (pid != null && LogoBitmapCache.resId(context, pid) != 0)
    }
    val heroEstDp = when {
        dense -> 220f
        compact -> 150f
        else -> 320f
    }
    val heroDecodePx = remember(density, heroEstDp) {
        (heroEstDp * density * 2f).roundToInt().coerceIn(64, LogoBitmapCache.DEFAULT_MAX_PX)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape),
        color = Color.Transparent,
        shadowElevation = elevation ?: if (compact) 2.dp else 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Brush.linearGradient(listOf(primary, secondary)))
                .clickable(onClick = onClick)
        ) {
            if (hasLogo) {
                LogoOrMonogram(
                    monogram = card.monogram,
                    logoUrl = card.logoUrl,
                    containerColor = contentColor.copy(alpha = 0.16f),
                    contentColor = contentColor,
                    logoKey = card.presetId,
                    circle = false,
                    borderWhite = card.logoBorderWhite,
                    borderSize = card.logoBorderSize,
                    decodeMaxPx = heroDecodePx,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                    logoKey = card.presetId,
                                    borderWhite = card.logoBorderWhite,
                                    borderSize = card.logoBorderSize
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
                                        logoKey = card.presetId,
                                        borderWhite = card.logoBorderWhite,
                                        borderSize = card.logoBorderSize
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
                                    logoKey = card.presetId,
                                    borderWhite = card.logoBorderWhite,
                                    borderSize = card.logoBorderSize
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
    }
}