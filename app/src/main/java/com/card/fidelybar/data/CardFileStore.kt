package com.card.fidelybar.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CardStoreFile(
    val version: Int = 1,
    val cards: List<UserCard> = emptyList()
)

class CardFileStore(context: Context) {

    private val file: File = File(context.filesDir, "fidelybar_cards.json")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun load(): List<UserCard> {
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<CardStoreFile>(file.readText()).cards
        }.getOrDefault(emptyList())
    }

    fun save(cards: List<UserCard>) {
        runCatching {
            val sorted = cards.sortedWith(compareBy({ it.isFavorite.not() }, { it.sortOrder }, { it.createdAt }))
            file.writeText(json.encodeToString(CardStoreFile(cards = sorted)))
        }
    }

    fun exportText(): String? = runCatching {
        if (file.exists()) file.readText() else null
    }.getOrNull()
}