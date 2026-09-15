package com.card.fidelybar.data

import android.content.Context

class CardRepositoryImpl(private val store: CardFileStore) : CardRepository {
    override fun load(): List<UserCard> = store.load()

    override fun loadWithResult(): LoadResult = store.loadWithResult()

    override fun save(cards: List<UserCard>): Boolean = store.save(cards)

    override fun hasBackup(): Boolean = store.hasBackup()

    override fun decode(text: String): List<UserCard> = store.decode(text)

    override fun encodeCards(cards: List<UserCard>): String = store.encodeCards(cards)

    companion object {
        fun create(context: Context): CardRepositoryImpl =
            CardRepositoryImpl(CardFileStore(context))
    }
}