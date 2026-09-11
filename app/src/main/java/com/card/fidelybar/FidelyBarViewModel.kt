package com.card.fidelybar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.CardFileStore
import com.card.fidelybar.data.LogoCache
import com.card.fidelybar.data.StoreCatalog
import com.card.fidelybar.data.StorePreset
import com.card.fidelybar.data.UserCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class FidelyBarViewModel(app: Application) : AndroidViewModel(app) {

    private val store = CardFileStore(app)

    private val _cards = MutableStateFlow<List<UserCard>>(emptyList())
    val cards = _cards.asStateFlow()

    val presets: List<StorePreset> = StoreCatalog.all

    init {
        _cards.value = store.load()
        prefetchLogos()
    }

    private fun prefetchLogos() {
        viewModelScope.launch(Dispatchers.IO) {
            _cards.value.forEach { card ->
                card.logoUrl?.let { LogoCache.ensure(getApplication(), it) }
            }
        }
    }

    fun getCard(id: String): UserCard? = _cards.value.firstOrNull { it.id == id }

    fun getCardFlow(id: String) = _cards.asStateFlow()

    fun addCard(
        presetId: String?,
        title: String,
        number: String,
        format: BarcodeFormatType,
        primaryHex: String,
        secondaryHex: String,
        monogram: Char,
        logoUrl: String?
    ): UserCard {
        val now = System.currentTimeMillis()
        val card = UserCard(
            id = UUID.randomUUID().toString(),
            presetId = presetId,
            title = title.trim().ifEmpty { "Carta senza nome" },
            number = number.trim(),
            format = format,
            primaryColorHex = primaryHex,
            secondaryColorHex = secondaryHex,
            monogram = monogram,
            logoUrl = logoUrl,
            sortOrder = now.toInt(),
            createdAt = now,
            updatedAt = now
        )
        _cards.update { it + card }
        persist()
        return card
    }

    fun updateCard(card: UserCard) {
        _cards.update { list -> list.map { if (it.id == card.id) card.copy(updatedAt = System.currentTimeMillis()) else it } }
        persist()
    }

    fun deleteCard(id: String) {
        _cards.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    fun setFavorite(id: String, favorite: Boolean) {
        _cards.update { list ->
            list.map {
                if (it.id == id) it.copy(isFavorite = favorite, updatedAt = System.currentTimeMillis()) else it
            }
        }
        persist()
    }

    fun toggleFavorite(id: String) {
        _cards.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite, updatedAt = System.currentTimeMillis()) else it }
        }
        persist()
    }

    private fun persist() {
        viewModelScope.launch(Dispatchers.IO) {
            store.save(_cards.value)
        }
    }
}