package com.card.fidelybar.data

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CardStoreFile(
    val version: Int = 1,
    val cards: List<UserCard> = emptyList()
)

@Suppress("DEPRECATION")
class CardFileStore(context: Context) {

    private val file: File = File(context.filesDir, "fidelybar_cards.json")

    private val encryptedFile: EncryptedFile = EncryptedFile.Builder(
        context,
        file,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun load(): List<UserCard> {
        if (!file.exists()) return emptyList()
        return readEncrypted() ?: migrateLegacy() ?: emptyList()
    }

    private fun readEncrypted(): List<UserCard>? {
        return runCatching {
            encryptedFile.openFileInput().use { input ->
                val text = String(input.readBytes(), Charsets.UTF_8)
                json.decodeFromString<CardStoreFile>(text).cards
            }
        }.getOrNull()
    }

    private fun migrateLegacy(): List<UserCard>? {
        val cards = runCatching {
            json.decodeFromString<CardStoreFile>(file.readText()).cards
        }.getOrNull() ?: return null
        if (cards.isNotEmpty()) save(cards)
        return cards
    }

    fun save(cards: List<UserCard>) {
        runCatching {
            val sorted = cards.sortedWith(compareBy({ it.isFavorite.not() }, { it.sortOrder }, { it.createdAt }))
            val encoded = json.encodeToString(CardStoreFile(cards = sorted))
            encryptedFile.openFileOutput().use { output ->
                output.write(encoded.toByteArray(Charsets.UTF_8))
            }
        }
    }

    fun hasBackup(): Boolean = file.exists()

    fun decode(text: String): List<UserCard> =
        runCatching {
            json.decodeFromString<CardStoreFile>(text).cards
        }.getOrDefault(emptyList())

    fun encodeCards(cards: List<UserCard>): String =
        json.encodeToString(CardStoreFile(cards = cards))
}