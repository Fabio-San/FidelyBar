package com.card.fidelybar.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.barcode.BarcodeEngine
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.StoreCatalog
import com.card.fidelybar.data.StorePreset
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardCodeView
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.LogoPalette
import com.card.fidelybar.ui.components.customPalette
import com.card.fidelybar.ui.components.rememberImeVisible
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import androidx.core.graphics.get
import kotlin.time.Duration.Companion.milliseconds

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
    var monogram by rememberSaveable { mutableStateOf('\u03A6') }
    var logoUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var isCustomColor by rememberSaveable { mutableStateOf(true) }
    var logoBorderWhite by rememberSaveable { mutableStateOf(false) }
    var logoBorderSize by rememberSaveable { mutableIntStateOf(1) }
    var query by rememberSaveable { mutableStateOf("") }
    var storeChosen by rememberSaveable { mutableStateOf(false) }
    var showFormatPicker by rememberSaveable { mutableStateOf(false) }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var showScanner by rememberSaveable { mutableStateOf(false) }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf<String?>(null) }
    val editorScroll = rememberScrollState()

    LaunchedEffect(editingCard) {
        saving = false
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
        scanStatus = "Lettura del codice\u2026"
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
        monogram = '\u03A6'
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
        val effectiveMonogram = if (monogram.isLetter()) monogram else (effectiveTitle.firstOrNull() ?: '\u03A6')
        saving = true
        scope.launch {
            try {
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
                delay(380.milliseconds)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSaved()
            } finally {
                saving = false
            }
        }
    }

    fun resetNewCard() {
        title = ""
        number = ""
        presetId = null
        format = BarcodeFormatType.CODE_128
        primaryHex = customPalette.first().primary
        secondaryHex = customPalette.first().secondary
        monogram = '\u03A6'
        logoUrl = null
        isCustomColor = true
        logoBorderWhite = false
        logoBorderSize = 1
        query = ""
        storeChosen = false
        showFormatPicker = false
        showColorPicker = false
        showScanner = false
        step = 0
        saving = false
        scanStatus = null
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
        val imeVisible = rememberImeVisible().value
        val isImmersiveSearch = step == 0 && imeVisible && (isSearchFocused || query.isNotBlank())

        // Uscendo dalla ricerca immersiva (tastiera chiusa o query azzerata) torna
        // in cima così l'anteprima della carta e lo stepper sono di nuovo visibili.
        LaunchedEffect(isImmersiveSearch) {
            if (!isImmersiveSearch) editorScroll.animateScrollTo(0)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FidelyBackgroundBrush())
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .let {
                    if (step == 2) it.verticalScroll(editorScroll)
                    else it
                }
                .let { if (inSheet) it else it.navigationBarsPadding() }
                .let { if (inSheet) it else it.imePadding() }
                .padding(bottom = 100.dp)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (inSheet) it else it.statusBarsPadding() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (step == 0) onBack() else step -= 1
            }) {
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 18.dp)
                ) {
                    CardVisual(
                        card = previewCard,
                        onClick = { if (step == 0 && storeChosen) storeChosen = false },
                        elevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Badge "Cambia" visibile solo in step 0 con negozio scelto (S1)
                    if (step == 0 && storeChosen) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Cambia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

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
                            monogram = if (isCustom) '\u03A6' else monogram,
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
                            fallbackTitle = fallbackTitle,
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
        }

        // Scrim gradiente protettivo (S2) + barra pulsanti in BottomCenter
        if (step != 0 || storeChosen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Gradiente da trasparente a sfondo, per staccare visivamente i pulsanti (S2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (inSheet) it else it.navigationBarsPadding() }
                    .let { if (inSheet) it else it.imePadding() }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
            ) {
                when (step) {
                    0 -> AnimatedVisibility(
                        visible = storeChosen,
                        enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = 380f)) + fadeIn(),
                        exit = shrinkVertically(spring(dampingRatio = 0.85f, stiffness = 380f)) + fadeOut()
                    ) {
                        WizardActionButton(
                            text = "Avanti",
                            enabled = true,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                step = 1
                            }
                        )
                    }
                    1 -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                resetNewCard()
                                onBack()
                            },
                            enabled = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Text("Annulla", style = MaterialTheme.typography.titleMedium)
                        }
                        WizardActionButton(
                            text = "Avanti",
                            enabled = saveEnabled,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                step = 2
                            },
                            modifier = Modifier.weight(1.6f)
                        )
                    }
                    else -> if (editingCard == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    resetNewCard()
                                    onBack()
                                },
                                enabled = !saving,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                            ) {
                                Text("Annulla", style = MaterialTheme.typography.titleMedium)
                            }
                            Button(
                                onClick = { save() },
                                enabled = saveEnabled && !saving,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1.6f)
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
                                        "Aggiungi carta",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    } else {
                        Button(
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
                                    "Salva modifiche",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
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
                color = if (scanStatus.contains("Lettura")) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
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
                        textAlign = TextAlign.Center
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
    fallbackTitle: String,
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
    val context = LocalContext.current
    LaunchedEffect(presetId, logoUrl) {
        LogoPalette.extract(context, presetId, logoUrl)
    }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text(fallbackTitle) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
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
            text = "Se hai importato una foto del codice o della carta, i colori sono gi\u00E0 stati rilevati in automatico.",
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

private fun extractCardHex(bitmap: Bitmap): Pair<String, String>? {
    val w = 48
    val h = (48f * bitmap.height / bitmap.width).toInt().coerceIn(1, 48)
    val small = bitmap.scale(w, h, true)
    val counts = HashMap<Int, Int>()
    for (i in 0 until w * h) {
        val pixel = small[i % w, i / w]
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
