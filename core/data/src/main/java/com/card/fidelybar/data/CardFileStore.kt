package com.card.fidelybar.data

import android.content.Context
import android.util.Log
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

sealed class LoadResult {
    data class Success(val cards: List<UserCard>) : LoadResult()
    data object Empty : LoadResult()
    data class Migrated(val cards: List<UserCard>) : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}

@Suppress("DEPRECATION")
class CardFileStore(context: Context) {

    companion object {
        private const val TAG = "CardFileStore"
    }

    private val filesDir: File = context.filesDir
    private val file: File = File(filesDir, "fidelybar_cards.json")
    private val tempFile: File = File(filesDir, "fidelybar_cards.json.tmp")
    private val backupFile: File = File(filesDir, "fidelybar_cards.json.bak")

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
        return when (val result = loadWithResult()) {
            is LoadResult.Success -> result.cards
            is LoadResult.Migrated -> result.cards
            is LoadResult.Empty -> emptyList()
            is LoadResult.Error -> {
                Log.e(TAG, "Errore caricamento carte: ${result.message}", result.cause)
                emptyList()
            }
        }
    }

    fun loadWithResult(): LoadResult {
        if (!file.exists()) return LoadResult.Empty

        // Tentativo 1: leggi file criptato
        val encrypted = readEncrypted()
        if (encrypted != null) return encrypted

        // Tentativo 2: migra legacy (plain text)
        val migrated = migrateLegacy()
        if (migrated != null) return migrated

        // Tentativo 3: prova a leggere il file temp (crash durante scrittura precedente)
        val fromTemp = readFromTemp()
        if (fromTemp != null) {
            save(fromTemp)
            return LoadResult.Migrated(fromTemp)
        }

        // Tentativo 4: prova a recuperare dall'ultima copia valida (.bak)
        val fromBackup = readFromBackup()
        if (fromBackup != null) {
            save(fromBackup)
            return LoadResult.Migrated(fromBackup)
        }

        return LoadResult.Error("File corrotto: impossibile leggere le carte")
    }

    private fun readEncrypted(): LoadResult? {
        return try {
            encryptedFile.openFileInput().use { input ->
                val text = String(input.readBytes(), Charsets.UTF_8)
                val cards = json.decodeFromString<CardStoreFile>(text).cards
                LoadResult.Success(cards)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Lettura criptata fallita, tentativo legacy", e)
            null
        }
    }

    private fun migrateLegacy(): LoadResult? {
        return try {
            val cards = json.decodeFromString<CardStoreFile>(file.readText()).cards
            if (cards.isNotEmpty()) {
                save(cards)
                Log.i(TAG, "Migrati ${cards.size} carte da formato legacy")
                LoadResult.Migrated(cards)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun readFromTemp(): List<UserCard>? {
        if (!tempFile.exists()) return null
        return try {
            val cards = json.decodeFromString<CardStoreFile>(tempFile.readText()).cards
            if (cards.isNotEmpty()) cards else null
        } catch (e: Exception) {
            null
        } finally {
            tempFile.delete()
        }
    }

    private fun readFromBackup(): List<UserCard>? {
        if (!backupFile.exists()) return null
        return runCatching {
            backupFile.copyTo(file, overwrite = true)
            (readEncrypted() as? LoadResult.Success)?.cards
        }.getOrElse {
            Log.w(TAG, "Recupero da backup fallito", it)
            null
        }
    }

    fun save(cards: List<UserCard>): Boolean {
        return try {
            val sorted = cards.sortedWith(
                compareBy({ it.isFavorite.not() }, { it.sortOrder }, { it.createdAt })
            )
            val encoded = json.encodeToString(CardStoreFile(cards = sorted))

            // Step 1: scrivi su file temp (plain text come backup)
            tempFile.writeText(encoded, Charsets.UTF_8)

            // Step 1.5: conserva una copia cifrata dell'ultima versione valida
            if (file.exists()) {
                runCatching { file.copyTo(backupFile, overwrite = true) }
            }

            // Step 2: scrivi su file criptato. Se esiste già, cancellalo prima altrimenti openFileOutput lancia IOException
            if (file.exists()) {
                file.delete()
            }
            encryptedFile.openFileOutput().use { output ->
                output.write(encoded.toByteArray(Charsets.UTF_8))
            }

            // Step 3: elimina il file temp
            tempFile.delete()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante il salvataggio", e)
            // Se la scrittura criptata è fallita ma il temp esiste, il temp è il backup
            false
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
