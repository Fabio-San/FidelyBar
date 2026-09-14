package com.card.fidelybar.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.barcode.BarcodeEngine
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.StoreCatalog
import com.card.fidelybar.data.StorePreset
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardCodeView
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.LogoOrMonogram
import com.card.fidelybar.ui.components.LogoPalette
import com.card.fidelybar.ui.components.LogoColors
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class CodeEntryMode { TYPE, SCAN }

data class ColorChoice(val primary: String, val secondary: String, val label: String)

private val customPalette = listOf(
    ColorChoice("#5B5BD6", "#3D3DA8", "Indaco"),
    ColorChoice("#6366F1", "#4648D8", "Iris"),
    ColorChoice("#3B82F6", "#2563EB", "Blu"),
    ColorChoice("#0891B2", "#0E7490", "Ciano"),
    ColorChoice("#0E7C61", "#0A5C49", "Verde"),
    ColorChoice("#10B981", "#059669", "Smeraldo"),
    ColorChoice("#84CC16", "#65A30D", "Lime"),
    ColorChoice("#EAB308", "#CA8A04", "Oro"),
    ColorChoice("#F59E0B", "#D97706", "Ambra"),
    ColorChoice("#F97316", "#EA580C", "Arancione"),
    ColorChoice("#E05A47", "#B84332", "Corallo"),
    ColorChoice("#EF4444", "#DC2626", "Rosso"),
    ColorChoice("#F43F5E", "#E11D48", "Cremisi"),
    ColorChoice("#EC4899", "#DB2777", "Rosa"),
    ColorChoice("#DB2777", "#BE185D", "Fucsia"),
    ColorChoice("#A855F7", "#9333EA", "Viola"),
    ColorChoice("#8B5CF6", "#6D28D9", "Iris scuro"),
    ColorChoice("#4C1D95", "#3B0A72", "Borgogna"),
    ColorChoice("#64748B", "#475569", "Ardesia"),
    ColorChoice("#78716C", "#57534E", "Pietra"),
    ColorChoice("#7C2D12", "#5B1F0A", "Cioccolato"),
    ColorChoice("#14532D", "#0E3A20", "Foresta"),
    ColorChoice("#0C4A6E", "#082F49", "Oceano"),
    ColorChoice("#111827", "#030712", "Notte")
)

private fun hexColor(color: Int) = "#%06X".format(color and 0x00FFFFFF)

private val shadeFactors = listOf(0.42f, 0.58f, 0.74f, 0.88f, 1f, 1.14f, 1.32f)

private fun buildLogoChoices(c: LogoColors): List<ColorChoice> {
    val p = c.primary
    return listOf(
        ColorChoice(hexColor(p), hexColor(c.secondary), "Dal logo"),
        ColorChoice(hexColor(LogoPalette.scaled(p, 0.78f)), hexColor(LogoPalette.scaled(p, 1.18f)), "Dal logo scuro → chiaro"),
        ColorChoice(hexColor(c.recommendedPrimary), hexColor(c.recommendedSecondary), "Base consigliata")
    )
}

private fun gradientFor(base: Int, shade: Float, intensity: Float, darkToLight: Boolean): Pair<String, String> {
    val v = LogoPalette.scaled(base, shade)
    val primary = LogoPalette.scaled(v, if (darkToLight) intensity * 0.85f else intensity)
    val secondary = LogoPalette.scaled(v, if (darkToLight) intensity * 1.15f else intensity * 0.62f)
    return hexColor(primary) to hexColor(secondary)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    viewModel: FidelyBarViewModel,
    cardId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    inSheet: Boolean = false
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
    var logoBorderWhite by rememberSaveable { mutableStateOf(false) }
    var logoBorderSize by rememberSaveable { mutableStateOf(1) }
    var query by rememberSaveable { mutableStateOf("") }
    var storeChosen by rememberSaveable { mutableStateOf(false) }
    var showFormatPicker by rememberSaveable { mutableStateOf(false) }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var showScanner by rememberSaveable { mutableStateOf(false) }
    var step by rememberSaveable { mutableStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf<String?>(null) }
    val editorScroll = rememberScrollState()

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
        logoBorderWhite = c.logoBorderWhite
        logoBorderSize = c.logoBorderSize
        storeChosen = true
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
    val haptic = LocalHapticFeedback.current

    fun applyDetected(content: String, detectedFormat: BarcodeFormatType) {
        val value = content.trim()
        // Un EAN-13 che inizia con 0 è rilevato come UPC-A:
        // lo riportiamo a EAN-13 ripremettendo lo 0.
        if (detectedFormat == BarcodeFormatType.UPC_A) {
            number = "0$value"
            format = BarcodeFormatType.EAN13
        } else {
            number = value
            format = detectedFormat
        }
        scanStatus = null
    }

    fun applyScan(bitmap: Bitmap?) {
        if (bitmap == null) {
            scanStatus = "Nessuna immagine ricevuta. Riprova."
            return
        }
        scanStatus = "Lettura del codice…"
        scope.launch(Dispatchers.Default) {
            val result = BarcodeEngine.decode(bitmap)
            val cardColors = if (result != null && isCustom) extractCardHex(bitmap) else null
            withContext(Dispatchers.Main) {
                if (result != null) {
                    applyDetected(result.content, result.format)
                    if (cardColors != null) {
                        primaryHex = cardColors.first
                        secondaryHex = cardColors.second
                        isCustomColor = true
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                    scanStatus = "Codice non riconosciuto nella foto. Riprova o scrivilo a mano."
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showScanner = true
            scanStatus = null
        } else {
            scanStatus = "Permesso fotocamera negato. Abilitalo nelle impostazioni per scansionare il codice."
        }
    }

    fun handleCameraTap() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showScanner = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

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
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (step == 0) step = 1
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
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (step == 0) step = 1
    }

    fun save() {
        if (saving) return
        val normalized = BarcodeEngine.normalize(number, format)
        val effectiveTitle = title.trim().ifEmpty { "Carta senza nome" }
        val effectiveMonogram = if (monogram.isLetter()) monogram else (effectiveTitle.firstOrNull() ?: 'Φ')
        saving = true
        scope.launch {
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
                        logoUrl = if (isCustom) null else logoUrl,
                        logoBorderWhite = logoBorderWhite,
                        logoBorderSize = logoBorderSize
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
                    logoUrl = if (isCustom) null else logoUrl,
                    logoBorderWhite = logoBorderWhite,
                    logoBorderSize = logoBorderSize
                )
            }
            delay(380)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSaved()
        }
    }

    val fallbackTitle = if (isCustom) "Personalizzata" else StoreCatalog.byId[presetId]?.name ?: "Personalizzata"
    val previewCard = UserCard(
        id = "preview",
        presetId = if (isCustom) null else presetId,
        title = title.ifBlank { fallbackTitle },
        number = number,
        format = format,
        primaryColorHex = primaryHex,
        secondaryColorHex = secondaryHex,
        monogram = monogram,
        logoUrl = if (isCustom) null else logoUrl,
        logoBorderWhite = logoBorderWhite,
        logoBorderSize = logoBorderSize
    )

    val hasLogo = if (isCustom) {
        false
    } else {
        val pid = presetId
        logoUrl != null ||
            (pid != null && com.card.fidelybar.ui.components.LogoBitmapCache.resId(context, pid) != 0)
    }

        var isSearchFocused by remember { mutableStateOf(false) }
        val isImmersiveSearch = step == 0 && (isSearchFocused || query.isNotBlank())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FidelyBackgroundBrush())
                .verticalScroll(editorScroll)
                .let { if (inSheet) it else it.navigationBarsPadding() }
                .let { if (inSheet) it else it.imePadding() }
                .padding(bottom = 24.dp)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (inSheet) it else it.statusBarsPadding() }
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

        AnimatedVisibility(
            visible = !isImmersiveSearch,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = spring(dampingRatio = 0.9f, stiffness = 400f)),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = spring(dampingRatio = 0.9f, stiffness = 400f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CardVisual(
                    card = previewCard,
                    onClick = {},
                    elevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 18.dp)
                )

                WizardStepper(
                    current = step,
                    onNavigate = { target ->
                        if (target < step) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            step = target
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInHorizontally(tween(240)) { it / 12 })
                        .togetherWith(
                            fadeOut(tween(120)) + slideOutHorizontally(tween(180)) { -it / 12 }
                        )
                },
                label = "wizardStep"
            ) { currentStep ->
                when (currentStep) {
                    0 -> StorePickerSection(
                        storeChosen = storeChosen,
                            isCustom = isCustom,
                            monogram = if (isCustom) 'Φ' else monogram,
                            logoUrl = if (isCustom) null else logoUrl,
                            name = fallbackTitle,
                            subtitle = format.label,
                            primaryHex = primaryHex,
                            storeId = if (isCustom) null else presetId,
                            customPalettePrimary = customPalette.first().primary,
                            query = query,
                            onQueryChange = { query = it },
                            onFocusChanged = { isSearchFocused = it },
                            onSelectPreset = { selectPreset(it) },
                            onSelectCustom = { selectCustom() },
                            onChange = { storeChosen = false }
                        )
                    1 -> WizardStepScroll {
                        CodeStepContent(
                            number = number,
                            format = format,
                            onNumberChange = { raw ->
                                number = when (format) {
                                    BarcodeFormatType.QR_CODE -> raw
                                    else -> raw.filter { it.isDigit() }.take(BarcodeEngine.expectedDigits(format) ?: 40)
                                }
                            },
                            onFormatChange = { format = it },
                            showFormatPicker = showFormatPicker,
                            onToggleFormatPicker = { showFormatPicker = !showFormatPicker },
                            scanStatus = scanStatus,
                            validationError = validationError,
                            onCameraClick = { handleCameraTap() },
                            onGalleryClick = { galleryLauncher.launch("image/*") }
                        )
                    }
                    else -> WizardStepScroll(scrollState = editorScroll) {
                        DetailsStepContent(
                            title = title,
                            onTitleChange = { title = it },
                            primaryHex = primaryHex,
                            secondaryHex = secondaryHex,
                            monogram = monogram,
                            previewTitle = title.ifBlank { fallbackTitle },
                            presetId = if (isCustom) null else presetId,
                            logoUrl = if (isCustom) null else logoUrl,
                            hasLogo = hasLogo,
                            logoBorderWhite = logoBorderWhite,
                            onLogoBorderChange = {
                                logoBorderWhite = it
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            logoBorderSize = logoBorderSize,
                            onLogoBorderSizeChange = { logoBorderSize = it },
                            scrollState = editorScroll,
                            onOpenColorPicker = { showColorPicker = true }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (inSheet) it else it.navigationBarsPadding() }
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
        ) {
            when (step) {
                0 -> WizardActionButton(
                    text = "Avanti",
                    enabled = storeChosen,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        step = 1
                    }
                )
                1 -> WizardActionButton(
                    text = "Avanti",
                    enabled = saveEnabled,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        step = 2
                    }
                )
                else -> Button(
                    onClick = { save() },
                    enabled = saveEnabled && !saving,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            if (editingCard != null) "Salva modifiche" else "Aggiungi carta",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (showScanner) {
        BarcodeScannerOverlay(
            onResult = { content, detectedFormat ->
                showScanner = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                applyDetected(content, detectedFormat)
            },
            onCancel = { showScanner = false }
        )
    }

    if (showColorPicker) {
ColorPickerScreen(
            primaryHex = primaryHex,
            secondaryHex = secondaryHex,
            monogram = monogram,
            title = title.ifBlank { fallbackTitle },
            presetId = if (isCustom) null else presetId,
            logoUrl = if (isCustom) null else logoUrl,
            logoBorderWhite = logoBorderWhite,
            logoBorderSize = logoBorderSize,
            onSelect = { choice ->
                primaryHex = choice.primary
                secondaryHex = choice.secondary
                isCustomColor = true
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

private enum class StepState { DONE, ACTIVE, TODO }

@Composable
private fun WizardStepper(current: Int, onNavigate: (Int) -> Unit) {
    val labels = listOf("Negozio", "Codice", "Aspetto")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { i, label ->
            val state = when {
                current > i -> StepState.DONE
                current == i -> StepState.ACTIVE
                else -> StepState.TODO
            }
            StepDot(
                index = i,
                label = label,
                state = state,
                onClick = if (state == StepState.DONE) ({ onNavigate(i) }) else null
            )
            if (i < labels.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (current > i) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun StepDot(index: Int, label: String, state: StepState, onClick: (() -> Unit)?) {
    val bg = when (state) {
        StepState.ACTIVE -> MaterialTheme.colorScheme.primary
        StepState.DONE -> MaterialTheme.colorScheme.primaryContainer
        StepState.TODO -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when (state) {
        StepState.ACTIVE -> MaterialTheme.colorScheme.onPrimary
        StepState.DONE -> MaterialTheme.colorScheme.onPrimaryContainer
        StepState.TODO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .let { if (onClick != null) it.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick) else it }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            if (state == StepState.DONE) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
            } else {
                Text(
                    (index + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (state == StepState.ACTIVE) FontWeight.Bold else FontWeight.Normal,
            color = if (state == StepState.TODO) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WizardStepScroll(
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
    ) {
        content()
    }
}

@Composable
private fun WizardActionButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CodeStepContent(
    number: String,
    format: BarcodeFormatType,
    onNumberChange: (String) -> Unit,
    onFormatChange: (BarcodeFormatType) -> Unit,
    showFormatPicker: Boolean,
    onToggleFormatPicker: () -> Unit,
    scanStatus: String?,
    validationError: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = number,
                onValueChange = onNumberChange,
                label = { Text("Numero della carta") },
                isError = validationError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (format == BarcodeFormatType.QR_CODE) KeyboardType.Text
                    else KeyboardType.NumberPassword
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            FilledTonalIconButton(
                onClick = onCameraClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.offset(y = 4.dp)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Scansiona codice")
            }
            FilledTonalIconButton(
                onClick = onGalleryClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.offset(y = 4.dp)
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = "Galleria")
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = validationError ?: when (format) {
                    BarcodeFormatType.EAN13, BarcodeFormatType.UPC_A -> "12 o 13 cifre"
                    BarcodeFormatType.EAN8 -> "7 o 8 cifre"
                    else -> "Formato: ${format.label}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (validationError != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onToggleFormatPicker) {
                Text("Modifica tipo codice")
            }
        }
        if (showFormatPicker) {
            Spacer(Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BarcodeFormatType.entries.forEach { f ->
                    val selected = format == f
                    FilterChip(
                        selected = selected,
                        onClick = { onFormatChange(f) },
                        label = { Text(f.label) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
        scanStatus?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (scanStatus?.contains("Lettura") == true) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        if (number.isNotBlank() && validationError == null) {
            CardCodeView(
                card = UserCard(
                    id = "preview",
                    title = "Anteprima",
                    number = number,
                    format = format
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatForDisplay(number.trim(), format),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                        text = "Inserisci o scansiona il numero per vedere il codice",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsStepContent(
    title: String,
    onTitleChange: (String) -> Unit,
    primaryHex: String,
    secondaryHex: String,
    monogram: Char,
    previewTitle: String,
    presetId: String?,
    logoUrl: String?,
    hasLogo: Boolean,
    logoBorderWhite: Boolean,
    onLogoBorderChange: (Boolean) -> Unit,
    logoBorderSize: Int,
    onLogoBorderSizeChange: (Int) -> Unit,
    scrollState: ScrollState,
    onOpenColorPicker: () -> Unit
) {
    LaunchedEffect(logoBorderWhite) {
        if (logoBorderWhite) {
            scrollState.scrollTo((scrollState.value + 180).coerceAtMost(scrollState.maxValue))
        }
    }
    // Pre-riscalda l'estrazione dei colori dal logo: così aprire il color picker
    // non deve aspettare decode + quantizzazione (LogoPalette tiene un cache MRU).
    val context = LocalContext.current
    LaunchedEffect(presetId, logoUrl) {
        LogoPalette.extract(context, presetId, logoUrl)
    }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Nome della carta") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Colore",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        ColorPickerTile(
            primaryHex = primaryHex,
            secondaryHex = secondaryHex,
            monogram = monogram,
            title = previewTitle,
            presetId = presetId,
            logoUrl = logoUrl,
            logoBorderWhite = logoBorderWhite,
            logoBorderSize = logoBorderSize,
            onClick = onOpenColorPicker
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Se hai importato una foto del codice o della carta, i colori sono già stati rilevati in automatico.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasLogo) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Bordo bianco attorno al logo",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Aiuta quando il colore di sfondo confonde le lettere del logo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = logoBorderWhite,
                            onCheckedChange = onLogoBorderChange
                        )
                    }
                    if (logoBorderWhite) {
                        Row(
                            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Spessore",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$logoBorderSize",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = logoBorderSize.toFloat(),
                            onValueChange = { onLogoBorderSizeChange(it.roundToInt()) },
                            valueRange = 1f..6f,
                            steps = 4,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatForDisplay(number: String, format: BarcodeFormatType): String =
    if (format == BarcodeFormatType.QR_CODE) number
    else number.chunked(4).joinToString(" ")

private fun extractCardHex(bitmap: Bitmap): Pair<String, String>? {
    val w = 48
    val h = (48f * bitmap.height / bitmap.width).toInt().coerceIn(1, 48)
    val small = Bitmap.createScaledBitmap(bitmap, w, h, true)
    val counts = HashMap<Int, Int>()
    for (i in 0 until w * h) {
        val pixel = small.getPixel(i % w, i / w)
        val quantized = pixel and 0xF0F0F0
        counts[quantized] = (counts[quantized] ?: 0) + 1
    }
    small.recycle()
    for ((color, _) in counts.entries.sortedByDescending { it.value }) {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl)
        if (hsl[1] >= 0.15f && hsl[2] in 0.12f..0.88f) {
            return "#%06X".format(color and 0xFFFFFF) to "#%06X".format(darkenRgb(color) and 0xFFFFFF)
        }
    }
    return null
}

private fun darkenRgb(rgb: Int): Int {
    val r = (((rgb shr 16) and 0xFF) * 0.74f).toInt()
    val g = (((rgb shr 8) and 0xFF) * 0.74f).toInt()
    val b = ((rgb and 0xFF) * 0.74f).toInt()
    return android.graphics.Color.rgb(r, g, b)
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
private fun StorePickerSection(
    storeChosen: Boolean,
    isCustom: Boolean,
    monogram: Char,
    logoUrl: String?,
    name: String,
    subtitle: String,
    primaryHex: String,
    storeId: String?,
    customPalettePrimary: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onSelectPreset: (StorePreset) -> Unit,
    onSelectCustom: () -> Unit,
    onChange: () -> Unit
) {
    AnimatedContent(
        targetState = storeChosen,
        transitionSpec = {
            (fadeIn(tween(200)) + slideInHorizontally(tween(240)) { it / 8 })
                .togetherWith(
                    fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { -it / 8 }
                )
        },
        label = "storePicker"
    ) { chosen ->
        if (chosen) {
            SelectedStoreBar(
                monogram = monogram,
                logoUrl = logoUrl,
                name = name,
                subtitle = subtitle,
                primaryHex = primaryHex,
                isCustom = isCustom,
                storeId = storeId,
                onChange = onChange
            )
        } else {
            WizardStepScroll {
                Box(
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp).fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Cerca negozio…") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { onFocusChanged(it.isFocused) }
                    )
                }
                val q = query.trim().lowercase()
                val featuredIds = listOf("conad", "coop", "eurospin", "trony", "crai", "md", "cfadda")
                if (q.isEmpty()) {
                    Text(
                        text = "Più richieste",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (q.isEmpty() || "personalizzata".contains(q) || "su misura".contains(q)) {
                        StoreChip(
                            label = "Personalizzata",
                            monogram = 'Φ',
                            primaryHex = customPalettePrimary,
                            selected = false,
                            onClick = onSelectCustom
                        )
                    }
                    StoreCatalog.all.forEach { preset ->
                        val matches = if (q.isEmpty()) {
                            preset.id in featuredIds
                        } else {
                            preset.name.lowercase().contains(q) ||
                                preset.searchAliases.any { it.lowercase().contains(q) }
                        }
                        if (matches) {
                            StoreChip(
                                label = preset.name,
                                monogram = preset.monogram,
                                primaryHex = preset.primaryColorHex,
                                selected = false,
                                logoKey = preset.id,
                                onClick = { onSelectPreset(preset) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Poi potrai cambiare ente in qualsiasi momento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
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
    val color = Color(primaryHex.toColorInt())
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
private fun StoreChip(
    label: String,
    monogram: Char,
    primaryHex: String,
    selected: Boolean,
    logoKey: String? = null,
    onClick: () -> Unit
) {
    val color = Color(primaryHex.toColorInt())
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
private fun ColorPickerTile(
    primaryHex: String,
    secondaryHex: String,
    monogram: Char,
    title: String,
    presetId: String?,
    logoUrl: String?,
    logoBorderWhite: Boolean = false,
    logoBorderSize: Int = 1,
    onClick: () -> Unit
) {
    val primary = Color(primaryHex.toColorInt())
    val secondary = Color(secondaryHex.toColorInt())
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(primary, secondary))),
                contentAlignment = Alignment.Center
            ) {
                LogoOrMonogram(
                    monogram = monogram,
                    logoUrl = logoUrl,
                    containerColor = Color.White.copy(alpha = 0.25f),
                    contentColor = Color.White,
                    size = 40,
                    logoKey = presetId,
                    borderWhite = logoBorderWhite,
                    borderSize = logoBorderSize,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(primary, secondary)))
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
            Column {
                Text(
                    "Scegli colore",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Apri la palette e cambia il colore in tempo reale",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerScreen(
    primaryHex: String,
    secondaryHex: String,
    monogram: Char,
    title: String,
    presetId: String?,
    logoUrl: String?,
    logoBorderWhite: Boolean = false,
    logoBorderSize: Int = 1,
    onSelect: (ColorChoice) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var logoColors by remember { mutableStateOf<LogoColors?>(null) }
    LaunchedEffect(presetId, logoUrl) {
        logoColors = LogoPalette.extract(context, presetId, logoUrl)
    }
    val logoChoices = remember(logoColors) { logoColors?.let { buildLogoChoices(it) }.orEmpty() }
    val recommendedChoice = logoChoices.getOrNull(2)

    var shadeBase by remember { mutableStateOf<ColorChoice?>(null) }

    val previewCard = UserCard(
        id = "color-preview",
        title = title,
        number = "0000000000000",
        format = BarcodeFormatType.EAN13,
        primaryColorHex = primaryHex,
        secondaryColorHex = secondaryHex,
        monogram = monogram,
        presetId = presetId,
        logoUrl = logoUrl,
        logoBorderWhite = logoBorderWhite,
        logoBorderSize = logoBorderSize
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
                label = "colorPage"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                        Text(
                            "Scegli colore",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        CardVisual(
                            card = previewCard,
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(24.dp))
                        if (logoChoices.isNotEmpty()) {
                            Text(
                                "Colori dal logo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                logoChoices.forEach { choice ->
                                    val isRecommended = choice == recommendedChoice
                                    Column {
                                        Swatch(
                                            choice = choice,
                                            selected = shadeBase?.primary == choice.primary,
                                            size = 42,
                                            onClick = { shadeBase = choice }
                                        )
                                        if (isRecommended && shadeBase?.primary != choice.primary) {
                                            Text(
                                                "Consigliata",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                        Text(
                            "Altri colori",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            customPalette.forEach { choice ->
                                Swatch(
                                    choice = choice,
                                    selected = shadeBase?.primary == choice.primary,
                                    size = 42,
                                    onClick = { shadeBase = choice }
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = onDismiss,
                            icon = { Icon(Icons.Filled.Check, contentDescription = null) },
                            text = { Text("Fatto", style = MaterialTheme.typography.titleMedium) },
                            shape = RoundedCornerShape(28.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    shadeBase?.let { base ->
        ColorShadeDialog(
            base = base,
            monogram = monogram,
            title = title,
            presetId = presetId,
            logoUrl = logoUrl,
            logoBorderWhite = logoBorderWhite,
            logoBorderSize = logoBorderSize,
            onConfirm = { choice ->
                onSelect(choice)
                shadeBase = null
            },
            onDismiss = { shadeBase = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorShadeDialog(
    base: ColorChoice,
    monogram: Char,
    title: String,
    presetId: String?,
    logoUrl: String?,
    logoBorderWhite: Boolean,
    logoBorderSize: Int,
    onConfirm: (ColorChoice) -> Unit,
    onDismiss: () -> Unit
) {
    var shade by remember { mutableStateOf(1f) }
    var intensity by remember { mutableStateOf(1f) }
    var darkToLight by remember { mutableStateOf(false) }

    val (tonePrimary, toneSecondary) = gradientFor(base.primary.toColorInt(), shade, intensity, darkToLight)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
                label = "shadeDialog"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tonalità di ${base.label}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Scegli la sfumatura, regola l'intensità e controlla subito l'anteprima.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    CardVisual(
                        card = UserCard(
                            id = "shade-preview",
                            title = title,
                            number = "0000000000000",
                            format = BarcodeFormatType.EAN13,
                            primaryColorHex = tonePrimary,
                            secondaryColorHex = toneSecondary,
                            monogram = monogram,
                            presetId = presetId,
                            logoUrl = logoUrl,
                            logoBorderWhite = logoBorderWhite,
                            logoBorderSize = logoBorderSize
                        ),
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Sfumature",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        shadeFactors.forEach { factor ->
                            val variant = LogoPalette.scaled(base.primary.toColorInt(), factor)
                            val isSel = abs(shade - factor) < 0.01f
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(variant))
                                    .border(
                                        width = if (isSel) 3.dp else 0.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { shade = factor },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Intensità",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${(intensity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        valueRange = 0.5f..1.6f,
                        steps = 10
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = darkToLight,
                            onCheckedChange = { darkToLight = it }
                        )
                        Column {
                            Text(
                                "Scuro → più chiaro",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Gradiente come i colori suggeriti per alcuni negozi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text("Annulla", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = {
                                onConfirm(ColorChoice(tonePrimary, toneSecondary, base.label))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text("Conferma", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun Swatch(
    choice: ColorChoice,
    selected: Boolean,
    size: Int = 38,
    onClick: () -> Unit
) {
    val color = Color(choice.primary.toColorInt())
    Box(
        modifier = Modifier
            .size(size.dp)
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
                modifier = Modifier.size((size * 0.47f).dp)
            )
        }
    }
}