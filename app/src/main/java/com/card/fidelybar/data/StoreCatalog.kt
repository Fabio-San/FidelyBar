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
        StorePreset("ligabue", "Ligabue", BarcodeFormatType.EAN13, "#8E9EAB", "#6E7E8B", 'L', hintDigits = 13),
        StorePreset("risparmiocasa", "Risparmio Casa", BarcodeFormatType.EAN13, "#E8600A", "#B24A08", 'R', hintDigits = 13),
        StorePreset("despar", "Despar", BarcodeFormatType.EAN13, "#D00015", "#8F0010", 'D', hintDigits = 13),
        StorePreset("sigma", "Sigma", BarcodeFormatType.EAN13, "#F7B500", "#C68E00", 'S', hintDigits = 13),
        StorePreset("crai", "CRAI", BarcodeFormatType.EAN13, "#002F6C", "#002050", 'C', hintDigits = 13),
        StorePreset("ins", "IN's Mercato", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'I', hintDigits = 13),
        StorePreset("famila", "Famila", BarcodeFormatType.EAN13, "#FFD100", "#D9AB00", 'F', hintDigits = 13),
        StorePreset("md", "MD", BarcodeFormatType.EAN13, "#CC0000", "#9E0000", 'M', hintDigits = 13),
        StorePreset("unes", "UNES", BarcodeFormatType.EAN13, "#008A3B", "#00672C", 'U', hintDigits = 13),
        StorePreset("unieuro", "Unieuro", BarcodeFormatType.CODE_128, "#002E6E", "#001F4B", 'U'),
        StorePreset("euronics", "Euronics", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'E', hintDigits = 13),
        StorePreset("trony", "Trony", BarcodeFormatType.EAN13, "#00AEEF", "#0084B0", 'T', hintDigits = 13),
        StorePreset("expert", "Expert", BarcodeFormatType.EAN13, "#0056A0", "#003D72", 'E', hintDigits = 13),
        StorePreset("bricoio", "Brico Io", BarcodeFormatType.EAN13, "#78A630", "#58801F", 'B', hintDigits = 13),
        StorePreset("bricoman", "Bricoman", BarcodeFormatType.EAN13, "#E2001A", "#A70014", 'B', hintDigits = 13),
        StorePreset("castorama", "Castorama", BarcodeFormatType.EAN13, "#F7891D", "#C66A10", 'C', hintDigits = 13),
        StorePreset("bricocenter", "Bricocenter", BarcodeFormatType.EAN13, "#F58220", "#C86613", 'B', hintDigits = 13),
        StorePreset("cisalfa", "Cisalfa Sport", BarcodeFormatType.EAN13, "#002B5C", "#001E40", 'C', hintDigits = 13),
        StorePreset("sportler", "Sportler", BarcodeFormatType.EAN13, "#E2001A", "#A70014", 'S', hintDigits = 13),
        StorePreset("hm", "H&M", BarcodeFormatType.EAN13, "#E2001A", "#A70014", 'H', hintDigits = 13),
        StorePreset("mango", "Mango", BarcodeFormatType.EAN13, "#231F20", "#111111", 'M', hintDigits = 13),
        StorePreset("ca", "C&A", BarcodeFormatType.EAN13, "#00539B", "#00375F", 'C', hintDigits = 13),
        StorePreset("uniqlo", "Uniqlo", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'U', hintDigits = 13),
        StorePreset("terranova", "Terranova", BarcodeFormatType.CODE_128, "#2B3990", "#1D2863", 'T'),
        StorePreset("pullbear", "Pull & Bear", BarcodeFormatType.CODE_128, "#0054A0", "#003B72", 'P'),
        StorePreset("bershka", "Bershka", BarcodeFormatType.CODE_128, "#1B1B1B", "#000000", 'B'),
        StorePreset("stradivarius", "Stradivarius", BarcodeFormatType.CODE_128, "#181818", "#000000", 'S'),
        StorePreset("oysho", "Oysho", BarcodeFormatType.CODE_128, "#E9006C", "#B00053", 'O'),
        StorePreset("massimodutti", "Massimo Dutti", BarcodeFormatType.CODE_128, "#3C3C3C", "#1F1F1F", 'M'),
        StorePreset("ao", "A&O", BarcodeFormatType.EAN13, "#1565C0", "#0D47A1", 'A', hintDigits = 13),
        StorePreset("ali", "Alì", BarcodeFormatType.EAN13, "#008A3B", "#006C2D", 'A', hintDigits = 13),
        StorePreset("aldi", "ALDI", BarcodeFormatType.EAN13, "#0057A3", "#003C73", 'A', hintDigits = 13),
        StorePreset("basko", "Basko", BarcodeFormatType.EAN13, "#E30613", "#A3050F", 'B', hintDigits = 13),
        StorePreset("coal", "COAL", BarcodeFormatType.EAN13, "#E30613", "#A3050F", 'C', hintDigits = 13),
        StorePreset("deco", "Decò", BarcodeFormatType.EAN13, "#F58220", "#C86613", 'D', hintDigits = 13),
        StorePreset("doro", "Doro", BarcodeFormatType.EAN13, "#D40000", "#9C0000", 'D', hintDigits = 13),
        StorePreset("dpiu", "DPiù", BarcodeFormatType.EAN13, "#1565C0", "#0D47A1", 'D', hintDigits = 13),
        StorePreset("ekom", "Ekom", BarcodeFormatType.EAN13, "#FF6A00", "#CC5500", 'E', hintDigits = 13),
        StorePreset("eataly", "Eataly", BarcodeFormatType.EAN13, "#00923F", "#007234", 'E', hintDigits = 13),
        StorePreset("gigante", "Il Gigante", BarcodeFormatType.EAN13, "#E30613", "#A3050F", 'G', hintDigits = 13),
        StorePreset("iper", "Iper", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'I', hintDigits = 13),
        StorePreset("iperal", "Iperal", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'I', hintDigits = 13),
        StorePreset("ipertosano", "IperTosano", BarcodeFormatType.EAN13, "#E30613", "#A3050F", 'T', hintDigits = 13),
        StorePreset("maxi", "Maxì", BarcodeFormatType.EAN13, "#1565C0", "#0D47A1", 'M', hintDigits = 13),
        StorePreset("mercato", "Mercatò", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'M', hintDigits = 13),
        StorePreset("migross", "Migross", BarcodeFormatType.EAN13, "#0071BC", "#00538A", 'M', hintDigits = 13),
        StorePreset("mpreis", "MPreis", BarcodeFormatType.EAN13, "#E2001A", "#A70014", 'M', hintDigits = 13),
        StorePreset("oasitigre", "Oasi Tigre", BarcodeFormatType.EAN13, "#F7B500", "#C68E00", 'T', hintDigits = 13),
        StorePreset("panorama", "Panorama", BarcodeFormatType.EAN13, "#004A9F", "#00306B", 'P', hintDigits = 13),
        StorePreset("penny", "Penny Market", BarcodeFormatType.EAN13, "#E2001A", "#A70014", 'P', hintDigits = 13),
        StorePreset("poli", "Poli", BarcodeFormatType.EAN13, "#0057A3", "#003C73", 'P', hintDigits = 13),
        StorePreset("prixquality", "Prix Quality", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'P', hintDigits = 13),
        StorePreset("rossetto", "Rossetto", BarcodeFormatType.EAN13, "#D40000", "#9C0000", 'R', hintDigits = 13),
        StorePreset("rossotono", "RossoTono", BarcodeFormatType.EAN13, "#E30613", "#A3050F", 'R', hintDigits = 13),
        StorePreset("sidis", "Sidis", BarcodeFormatType.EAN13, "#E4002B", "#A80020", 'S', hintDigits = 13),
        StorePreset("sogegross", "Sogegross", BarcodeFormatType.EAN13, "#008A3B", "#006C2D", 'S', hintDigits = 13),
        StorePreset("sole365", "Sole 365", BarcodeFormatType.EAN13, "#F5A800", "#C98800", 'S', hintDigits = 13),
        StorePreset("todis", "Todis", BarcodeFormatType.EAN13, "#F58220", "#C86613", 'T', hintDigits = 13),
        StorePreset("tuodi", "Tuodì", BarcodeFormatType.EAN13, "#008A3B", "#006C2D", 'T', hintDigits = 13),
        StorePreset("cfadda", "CFadda", BarcodeFormatType.EAN13, "#FF6600", "#CC5200", 'C', hintDigits = 13)
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