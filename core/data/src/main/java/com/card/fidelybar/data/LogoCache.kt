package com.card.fidelybar.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cache persistente dei loghi remoti.
 * Il logo viene scaricato una sola volta in filesDir/logos/ e da quel
 * momento viene sempre servito dal file locale (offline).
 */
object LogoCache {

    private const val MAX_BYTES = 2 * 1024 * 1024

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val SUPPORTED_EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "gif")

    fun dir(context: Context): File = File(context.filesDir, "logos")

    /** Restituisce il file già scaricato, oppure null. */
    fun fileFor(context: Context, url: String): File? {
        val name = sanitize(url) ?: return null
        val file = File(dir(context), name)
        return if (file.exists()) file else null
    }

    /** Scarica il logo se non è ancora in cache, restituendo il file locale. */
    suspend fun ensure(context: Context, url: String): File? = withContext(Dispatchers.IO) {
        fileFor(context, url)?.let { return@withContext it }
        val name = sanitize(url) ?: return@withContext null
        // Solo connessioni HTTPS: niente loghi (o redirect) su plain HTTP.
        if (!url.startsWith("https://")) return@withContext null
        runCatching {
            dir(context).mkdirs()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body ?: return@runCatching null
                // Limita lo stream a MAX_BYTES: evita download enormi da server malevoli.
                if (body.contentLength() > MAX_BYTES) return@runCatching null
                val bytes = body.bytes()
                if (bytes.isEmpty() || bytes.size > MAX_BYTES) return@runCatching null
                val file = File(dir(context), name)
                file.writeBytes(bytes)
                file
            }
        }.getOrNull()
    }

    private fun sanitize(url: String): String? = runCatching {
        val path = java.net.URI(url).path
        val last = path.substringAfterLast('/').substringBefore('?')
        if (last.isBlank()) return@runCatching null
        val base = last.substringBeforeLast('.').take(40).ifBlank { "logo" }
        val ext = last.substringAfterLast('.', "").lowercase().take(8)
        val safeExt = if (ext in SUPPORTED_EXTENSIONS) ext else "png"
        val digest = MessageDigest.getInstance("MD5")
            .digest(url.toByteArray())
            .take(4)
            .joinToString("") { "%02x".format(it) }
        "$base-$digest.$safeExt"
    }.getOrNull()
}