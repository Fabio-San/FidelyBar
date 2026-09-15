package com.card.fidelybar.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import com.card.fidelybar.data.LogoCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.LinkedHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LogoColors(
    val primary: Int,
    val secondary: Int,
    val accent: Int,
    val recommendedPrimary: Int,
    val recommendedSecondary: Int,
    val transparentBg: Boolean = false
)

object LogoPalette {

    private val dispatcher: CoroutineDispatcher =
        Executors.newFixedThreadPool(2) { r ->
            Thread(r, "logo-palette").apply { priority = Thread.NORM_PRIORITY }
        }.asCoroutineDispatcher()

    // Piccola MRU in memoria: estrarre i colori è la parte più "pesante" del
    // color picker (decode + quantizzazione). Teniamo le ultime 48 estrazioni
    // così riaprire il picker sullo stesso negozio è istantaneo e senza scatti.
    private val cacheLimit = 48
    private val cache = object : LinkedHashMap<String, LogoColors>(cacheLimit, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LogoColors>): Boolean =
            size > cacheLimit
    }

    suspend fun extract(context: Context, logoKey: String?, logoUrl: String?): LogoColors? {
        val key = "$logoKey|$logoUrl"
        synchronized(cache) {
            cache[key]?.let { return it }
        }
        val result = withContext(dispatcher) { compute(context, logoKey, logoUrl) }
        if (result != null) {
            synchronized(cache) { cache[key] = result }
        }
        return result
    }

    private fun compute(context: Context, logoKey: String?, logoUrl: String?): LogoColors? {
        val bitmap = load(context, logoKey, logoUrl) ?: return null
        val (samples, transparentRatio) = sample(bitmap)
        if (samples.isEmpty()) return null
        val seeds = dominantSeeds(samples)
        if (seeds.isEmpty()) return null
        val c0 = seeds[0]
        val c1 = seeds.getOrNull(1) ?: darken(c0, 0.72f)
        val c2 = seeds.getOrNull(2) ?: tint(c0, 0.2f)
        val transparentBg = transparentRatio > 0.20f
        val (recommendedPrimary, recommendedSecondary) = recommended(transparentBg, c0)
        return LogoColors(c0, c1, c2, recommendedPrimary, recommendedSecondary, transparentBg)
    }

    private fun recommended(transparentBg: Boolean, c0: Int): Pair<Int, Int> {
        if (transparentBg) {
            // Logo senza sfondo: il colore dominante è quello del testo/glyph.
            // Per non fonderlo con lo sfondo della carta si suggerisce un neutro
            // con il massimo contrasto rispetto al colore del logo.
            return if (luma(c0) < 150) {
                0xFFE9EFF5.toInt() to 0xFFC3D0DE.toInt()
            } else {
                0xFF1F2A36.toInt() to 0xFF0E141B.toInt()
            }
        }
        val rec = darken(c0, 0.72f)
        return rec to darken(rec, 0.78f)
    }

    private fun luma(c: Int): Int {
        val r = c shr 16 and 0xFF
        val g = c shr 8 and 0xFF
        val b = c and 0xFF
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    private fun load(context: Context, logoKey: String?, logoUrl: String?): Bitmap? {
        if (logoKey != null && LogoBitmapCache.resId(context, logoKey) != 0) {
            return LogoBitmapCache.get(context, logoKey, 192)?.let { it.asAndroidBitmap() }
        }
        val file = logoUrl?.let { LogoCache.fileFor(context, it) }
        if (file != null) return runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
        return null
    }

    private fun sample(src: Bitmap): Pair<IntArray, Float> {
        val maxDim = 64
        val scale = maxDim.toFloat() / max(src.width, src.height).coerceAtLeast(1)
        val w = (src.width * scale).roundToInt().coerceAtLeast(1)
        val h = (src.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = if (w == src.width && h == src.height) src
        else Bitmap.createScaledBitmap(src, w, h, true)
        val px = IntArray(w * h)
        scaled.getPixels(px, 0, w, 0, 0, w, h)
        if (scaled !== src) scaled.recycle()
        val out = ArrayList<Int>(px.size)
        var transparent = 0
        val total = px.size
        for (p in px) {
            val a = p ushr 24 and 0xFF
            if (a < 32) {
                transparent++
                continue
            }
            if (a < 128) continue
            val r = p shr 16 and 0xFF
            val g = p shr 8 and 0xFF
            val b = p and 0xFF
            val maxC = max(r, max(g, b)); val minC = min(r, min(g, b))
            val luma = (r * 299 + g * 587 + b * 114) / 1000
            val sat = if (maxC == 0) 0f else (maxC - minC).toFloat() / maxC
            if (sat < 0.18f) continue
            if (luma < 24 || luma > 235) continue
            out.add(p)
        }
        return out.toIntArray() to transparent.toFloat() / total
    }

    private fun dominantSeeds(samples: IntArray): List<Int> {
        val hist = HashMap<Int, Int>()
        for (p in samples) {
            val r = p shr 16 and 0xFF
            val g = p shr 8 and 0xFF
            val b = p and 0xFF
            val key = (r shr 2) shl 10 or ((g shr 2) shl 5) or (b shr 2)
            hist[key] = (hist[key] ?: 0) + 1
        }
        val sorted = hist.entries.sortedByDescending { it.value }
        val seeds = ArrayList<Int>()
        for (e in sorted) {
            val key = e.key
            val r = (key shr 10 and 0x3F) shl 2 or 2
            val g = (key shr 5 and 0x3F) shl 2 or 2
            val b = (key and 0x3F) shl 2 or 2
            val rgb = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            if (seeds.none { dist(it, rgb) < 40f }) {
                seeds.add(rgb)
                if (seeds.size == 3) break
            }
        }
        return seeds
    }

    private fun dist(a: Int, b: Int): Float {
        val dr = (a shr 16 and 0xFF) - (b shr 16 and 0xFF)
        val dg = (a shr 8 and 0xFF) - (b shr 8 and 0xFF)
        val db = (a and 0xFF) - (b and 0xFF)
        return sqrt((dr * dr + dg * dg + db * db).toFloat())
    }

    private fun darken(color: Int, f: Float): Int {
        val r = ((color shr 16 and 0xFF) * f).roundToInt().coerceIn(0, 255)
        val g = ((color shr 8 and 0xFF) * f).roundToInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * f).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun tint(color: Int, f: Float): Int {
        val r = ((color shr 16 and 0xFF) + (255 - (color shr 16 and 0xFF)) * f).roundToInt().coerceIn(0, 255)
        val g = ((color shr 8 and 0xFF) + (255 - (color shr 8 and 0xFF)) * f).roundToInt().coerceIn(0, 255)
        val b = ((color and 0xFF) + (255 - (color and 0xFF)) * f).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    /** f < 1 scurisce, f > 1 schiarisce (satura verso il bianco). */
    fun scaled(color: Int, f: Float): Int {
        val r = ((color shr 16 and 0xFF) * f).roundToInt().coerceIn(0, 255)
        val g = ((color shr 8 and 0xFF) * f).roundToInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * f).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}
