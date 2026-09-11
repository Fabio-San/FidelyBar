package com.card.fidelybar.data

import kotlinx.serialization.Serializable

@Serializable
enum class BarcodeFormatType(val label: String) {
    EAN13("EAN-13"),
    EAN8("EAN-8"),
    UPC_A("UPC-A"),
    CODE_128("Code 128"),
    CODE_39("Code 39"),
    CODABAR("Codabar"),
    QR_CODE("QR Code")
}

@Serializable
data class StorePreset(
    val id: String,
    val name: String,
    val format: BarcodeFormatType,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val monogram: Char,
    val logoUrl: String? = null,
    val hintDigits: Int? = null
) {
    companion object {
        const val CUSTOM = "custom"
    }
}

@Serializable
data class UserCard(
    val id: String,
    val presetId: String? = null,
    val title: String,
    val number: String,
    val format: BarcodeFormatType,
    val primaryColorHex: String = "#006C4C",
    val secondaryColorHex: String = "#004D35",
    val monogram: Char = 'Φ',
    val logoUrl: String? = null,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)