package com.card.fidelybar.data

object StoreCatalog {

    val all: List<StorePreset> = listOf(
        StorePreset("esselunga", "Esselunga", BarcodeFormatType.CODE_128, "#00A550", "#00783B", 'E', hintDigits = 14),
        StorePreset("conad", "Conad", BarcodeFormatType.EAN13, "#F37021", "#D2541B", 'C', hintDigits = 13),
        StorePreset("coop", "Coop", BarcodeFormatType.EAN13, "#004C97", "#003366", 'C', hintDigits = 13),
        StorePreset("lidl", "Lidl Plus", BarcodeFormatType.QR_CODE, "#0050AA", "#1F3E8C", 'L'),
        StorePreset("eurospin", "Eurospin", BarcodeFormatType.EAN13, "#008C45", "#00672F", 'E', hintDigits = 13),
        StorePreset("pam", "Pam", BarcodeFormatType.EAN13, "#E30613", "#A3050F", 'P', hintDigits = 13),
        StorePreset("carrefour", "Carrefour", BarcodeFormatType.EAN13, "#004A9F", "#00306B", 'C', hintDigits = 13),
        StorePreset("bennet", "Bennet", BarcodeFormatType.EAN13, "#0071BC", "#00538A", 'B', hintDigits = 13),
        StorePreset("tigota", "Tigotà", BarcodeFormatType.EAN13, "#F5A800", "#C98800", 'T', hintDigits = 13),
        StorePreset("acquaesapone", "Acqua & Sapone", BarcodeFormatType.EAN13, "#009EE3", "#0077AE", 'A', hintDigits = 13),
        StorePreset("sephora", "Sephora", BarcodeFormatType.EAN13, "#161616", "#000000", 'S', hintDigits = 13),
        StorePreset("douglas", "Douglas", BarcodeFormatType.EAN13, "#7F4F96", "#5C3770", 'D', hintDigits = 13),
        StorePreset("decathlon", "Decathlon", BarcodeFormatType.EAN13, "#0082C3", "#005E8F", 'D', hintDigits = 13),
        StorePreset("ikea", "IKEA Family", BarcodeFormatType.EAN13, "#FFDA1A", "#D9B400", 'I', hintDigits = 13),
        StorePreset("feltrinelli", "Feltrinelli", BarcodeFormatType.EAN13, "#E4002B", "#B00020", 'F', hintDigits = 13),
        StorePreset("mondadori", "Mondadori", BarcodeFormatType.EAN13, "#E4202B", "#B6151E", 'M', hintDigits = 13),
        StorePreset("mediaworld", "MediaWorld", BarcodeFormatType.CODE_128, "#FFCB05", "#E0B000", 'M'),
        StorePreset("leroy", "Leroy Merlin", BarcodeFormatType.EAN13, "#F6E01E", "#D4C20A", 'L', hintDigits = 13),
        StorePreset("ovs", "OVS", BarcodeFormatType.EAN13, "#1C1C1C", "#000000", 'O', hintDigits = 13),
        StorePreset("zara", "Zara", BarcodeFormatType.EAN13, "#111111", "#000000", 'Z', hintDigits = 13),
        StorePreset("intimissimi", "Intimissimi", BarcodeFormatType.CODE_128, "#D078A0", "#B05A84", 'I'),
        StorePreset("calzedonia", "Calzedonia", BarcodeFormatType.CODE_128, "#6A8C4F", "#4E6A38", 'C'),
        StorePreset("tezenis", "Tezenis", BarcodeFormatType.CODE_128, "#C8102E", "#9C0C24", 'T'),
        StorePreset("ligabue", "Ligabue", BarcodeFormatType.EAN13, "#8E9EAB", "#6E7E8B", 'L', hintDigits = 13)
    )

    val byId: Map<String, StorePreset> = all.associateBy { it.id }

    sealed interface StoreChoice {
        data class Preset(val preset: StorePreset) : StoreChoice
        data object Custom : StoreChoice
    }

    fun fromIdOrCustom(id: String?): StoreChoice? = when (id) {
        null, StorePreset.CUSTOM -> StoreChoice.Custom
        else -> byId[id]?.let { StoreChoice.Preset(it) }
    }
}