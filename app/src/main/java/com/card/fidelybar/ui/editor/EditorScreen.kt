package com.card.fidelybar.ui.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.barcode.BarcodeEngine
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.StoreCatalog
import com.card.fidelybar.data.StorePreset
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardCodeView
import com.card.fidelybar.ui.components.LogoOrMonogram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class CodeEntryMode { TYPE, SCAN }

private data class ColorChoice(val primary: String, val secondary: String, val label: String)

private val customPalette = listOf(
    ColorChoice("#5B5BD6", "#3D3DA8", "Indaco"),
    ColorChoice("#0E7C61", "#0A5C49", "Verde"),
    ColorChoice("#E05A47", "#B84332", "Corallo"),
    ColorChoice("#8B5CF6", "#6D28D9", "Viola"),
    ColorChoice("#0891B2", "#0E7490", "Ciano"),
    ColorChoice("#D97706", "#B45309", "Ambra"),
    ColorChoice("#DB2777", "#BE185D", "Rosa"),
    ColorChoice("#475569", "#334155", "Ardesia")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    viewModel: FidelyBarViewModel,
    cardId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val editingCard = cardId?.let { id -> cards.firstOrNull { it.id == id } }

    var title by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var presetId by rememberSaveable { mutableStateOf<String?>(null) }
    var format by rememberSaveable { mutableStateOf(BarcodeFormatType.CODE_128) }
    var primaryHex by rememberSaveable { mutableStateOf(customPalette.first().primary) }
    var secondaryHex by rememberSaveable { mutableStateOf(customPalette.first().secondary) }
    var monogram by rememberSaveable { mutableStateOf('Φ') }
    var logoUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var isCustomColor by rememberSaveable { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var storeChosen by rememberSaveable { mutableStateOf(false) }
    var entryMode by rememberSaveable { mutableStateOf(CodeEntryMode.TYPE) }
    var scanStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editingCard) {
        val c = editingCard ?: return@LaunchedEffect
        title = c.title
        number = c.number
        presetId = c.presetId
        format = c.format
        primaryHex = c.primaryColorHex
        secondaryHex = c.secondaryColorHex
        monogram = c.monogram
        logoUrl = c.logoUrl
        isCustomColor = c.presetId == null || c.presetId == StorePreset.CUSTOM
        storeChosen = true
    }

    val filteredPresets = remember(query) {
        if (query.isBlank()) StoreCatalog.all
        else StoreCatalog.all.filter { it.name.lowercase().contains(query.lowercase()) }
    }

    val isCustom = presetId == null || presetId == StorePreset.CUSTOM

    var validationError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(number, format) {
        validationError = runCatching { BarcodeEngine.normalize(number, format) }
            .exceptionOrNull()
            ?.message
    }

    val saveEnabled = number.isNotBlank() && validationError == null

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun applyScan(bitmap: Bitmap?) {
        if (bitmap == null) {
            scanStatus = "Nessuna immagine ricevuta. Riprova."
            return
        }
        scanStatus = "Lettura del codice…"
        scope.launch(Dispatchers.Default) {
            val result = BarcodeEngine.decode(bitmap)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    number = result.content.trim()
                    format = result.format
                    entryMode = CodeEntryMode.TYPE
                    scanStatus = null
                } else {
                    scanStatus = "Codice non riconosciuto nella foto. Riprova o scrivilo a mano."
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> applyScan(bitmap) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 2 })
                }
                withContext(Dispatchers.Main) { applyScan(bitmap) }
            }
        }
    }

    fun selectPreset(preset: StorePreset) {
        presetId = preset.id
        format = preset.format
        primaryHex = preset.primaryColorHex
        secondaryHex = preset.secondaryColorHex
        monogram = preset.monogram
        logoUrl = preset.logoUrl
        isCustomColor = false
        storeChosen = true
        if (title.isBlank()) title = preset.name
    }

    fun selectCustom() {
        presetId = null
        format = BarcodeFormatType.CODE_128
        primaryHex = customPalette.first().primary
        secondaryHex = customPalette.first().secondary
        monogram = 'Φ'
        logoUrl = null
        isCustomColor = true
        storeChosen = true
        if (title.isBlank()) title = "La mia carta"
    }

    fun save() {
        val normalized = BarcodeEngine.normalize(number, format)
        val effectiveTitle = title.trim().ifEmpty { "Carta senza nome" }
        val effectiveMonogram = if (monogram.isLetter()) monogram else (effectiveTitle.firstOrNull() ?: 'Φ')

        if (editingCard != null) {
            viewModel.updateCard(
                editingCard.copy(
                    presetId = if (isCustom) null else presetId,
                    title = effectiveTitle,
                    number = normalized,
                    format = format,
                    primaryColorHex = primaryHex,
                    secondaryColorHex = secondaryHex,
                    monogram = effectiveMonogram,
                    logoUrl = if (isCustom) null else logoUrl
                )
            )
        } else {
            viewModel.addCard(
                presetId = if (isCustom) null else presetId,
                title = effectiveTitle,
                number = normalized,
                format = format,
                primaryHex = primaryHex,
                secondaryHex = secondaryHex,
                monogram = effectiveMonogram,
                logoUrl = if (isCustom) null else logoUrl
            )
        }
        onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
            }
            Text(
                text = if (editingCard != null) "Modifica carta" else "Nuova carta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            // ---------- STEP 1 · Ente ----------
            item {
                SectionHeader(text = "Negozio o programma")
            }

            if (storeChosen) {
                item {
                    SelectedStoreBar(
                        monogram = if (isCustom) 'Φ' else monogram,
                        logoUrl = if (isCustom) null else logoUrl,
                        name = if (isCustom) "Su misura" else StoreCatalog.byId[presetId]?.name ?: "Su misura",
                        subtitle = format.label,
                        primaryHex = primaryHex,
                        isCustom = isCustom,
                        storeId = if (isCustom) null else presetId,
                        onChange = { storeChosen = false }
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.padding(bottom = 6.dp).fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Cerca negozio…") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StoreChip(
                            label = "Su misura",
                            monogram = 'Φ',
                            primaryHex = customPalette.first().primary,
                            selected = false,
                            onClick = { selectCustom() }
                        )
                        filteredPresets.forEach { preset ->
                            StoreChip(
                                label = preset.name,
                                monogram = preset.monogram,
                                primaryHex = preset.primaryColorHex,
                                selected = false,
                                logoKey = preset.id,
                                onClick = { selectPreset(preset) }
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "Poi potrai cambiare ente in qualsiasi momento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
            }

            // ---------- STEP 2 · Codice ----------
            item {
                SectionHeader(text = "Codice carta")
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EntryOption(
                        title = "Scrivilo",
                        subtitle = "digitando sulla tastiera",
                        icon = Icons.Filled.Keyboard,
                        selected = entryMode == CodeEntryMode.TYPE,
                        onClick = { entryMode = CodeEntryMode.TYPE; scanStatus = null },
                        modifier = Modifier.weight(1f)
                    )
                    EntryOption(
                        title = "Da foto",
                        subtitle = "fotocamera o galleria",
                        icon = Icons.Filled.PhotoCamera,
                        selected = entryMode == CodeEntryMode.SCAN,
                        onClick = { entryMode = CodeEntryMode.SCAN; scanStatus = null },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (entryMode == CodeEntryMode.SCAN) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Scatta foto")
                        }
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Galleria")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    scanStatus?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (scanStatus?.contains("Lettura") == true) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (entryMode == CodeEntryMode.TYPE) {
                item {
                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BarcodeFormatType.entries.forEach { f ->
                            val selected = format == f
                            FilterChip(
                                selected = selected,
                                onClick = { format = f },
                                label = { Text(f.label) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = number,
                        onValueChange = { raw ->
                            number = when (format) {
                                BarcodeFormatType.QR_CODE -> raw
                                else -> raw.filter { it.isDigit() }.take(BarcodeEngine.expectedDigits(format) ?: 40)
                            }
                        },
                        label = { Text("Numero della carta") },
                        supportingText = {
                            Text(
                                validationError ?: when (format) {
                                    BarcodeFormatType.EAN13, BarcodeFormatType.UPC_A -> "12 o 13 cifre"
                                    BarcodeFormatType.EAN8 -> "7 o 8 cifre"
                                    else -> "Codice del programma fedeltà"
                                }
                            )
                        },
                        isError = validationError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (format == BarcodeFormatType.QR_CODE) KeyboardType.Text else KeyboardType.NumberPassword
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ---------- STEP 3 · Dettagli ----------
            item {
                SectionHeader(text = "Dettagli")
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome della carta") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isCustom && entryMode == CodeEntryMode.TYPE) {
                item {
                    Text(
                        "Colore",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        customPalette.forEach { choice ->
                            val selected = primaryHex == choice.primary
                            Swatch(choice = choice, selected = selected, onClick = {
                                primaryHex = choice.primary
                                secondaryHex = choice.secondary
                            })
                        }
                    }
                }
            }

            // ---------- Anteprima ----------
            item {
                SectionHeader(text = "Anteprima")
            }

            item {
                if (number.isNotBlank() && validationError == null) {
                    CardCodeView(
                        card = UserCard(
                            id = "preview",
                            title = title.ifBlank { "Anteprima" },
                            number = number,
                            format = format
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (entryMode == CodeEntryMode.SCAN) {
                                    "Seleziona foto o galleria per leggere il codice"
                                } else {
                                    "Inserisci o scansiona il numero per vedere il codice"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { save() },
                    enabled = saveEnabled,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        if (editingCard != null) "Salva modifiche" else "Aggiungi carta",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun SelectedStoreBar(
    monogram: Char,
    logoUrl: String?,
    name: String,
    subtitle: String,
    primaryHex: String,
    isCustom: Boolean,
    storeId: String?,
    onChange: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(primaryHex))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LogoOrMonogram(
                    monogram = monogram,
                    logoUrl = logoUrl,
                    containerColor = if (isCustom) MaterialTheme.colorScheme.primary else color,
                    contentColor = Color.White,
                    size = 34,
                    logoKey = storeId
                )
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        TextButton(onClick = onChange) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Cambia")
        }
    }
}

@Composable
private fun EntryOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StoreChip(
    label: String,
    monogram: Char,
    primaryHex: String,
    selected: Boolean,
    logoKey: String? = null,
    onClick: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(primaryHex))
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogoOrMonogram(
                monogram = monogram,
                logoUrl = null,
                containerColor = color.copy(alpha = 0.9f),
                contentColor = Color.White,
                size = 28,
                logoKey = logoKey
            )
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Swatch(
    choice: ColorChoice,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(choice.primary))
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = choice.label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}