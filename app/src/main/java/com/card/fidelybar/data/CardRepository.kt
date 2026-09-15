package com.card.fidelybar.data

interface CardRepository {
    fun load(): List<UserCard>
    fun loadWithResult(): LoadResult
    fun save(cards: List<UserCard>): Boolean
    fun hasBackup(): Boolean
    fun decode(text: String): List<UserCard>
    fun encodeCards(cards: List<UserCard>): String
}