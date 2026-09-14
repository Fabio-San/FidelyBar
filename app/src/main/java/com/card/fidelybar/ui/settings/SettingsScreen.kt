package com.card.fidelybar.ui.settings

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.CardCrypto
import com.card.fidelybar.data.CardFileStore
import com.card.fidelybar.data.FontScale
import com.card.fidelybar.data.ThemeMode
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.foregroundFor
import com.card.fidelybar.ui.components.rememberColor
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush
import com.card.fidelybar.ui.theme.LocalDarkTheme
import com.card.fidelybar.ui.theme.accentTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private data class ImportOffer(val cards: List<UserCard>)

private enum class PasswordAction { EXPORT_SHARE, EXPORT_SAVE, IMPORT }

private enum class SettingsSection(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
) {
    APPEARANCE(
        Icons.Filled.BrightnessAuto,
        "Aspetto",
        "Tema, dimensione carattere e anteprima"
    ),
    BACKUP(
        Icons.Filled.Backup,
        "Backup e ripristino",
        "Esporta, salva, ripristina ed elimina le carte"
    ),
    INFO(
        Icons.Filled.Info,
        "Info su FidelyBar",
        "Versione, privacy e attribuzioni"
    )
}

@Composable
fun SettingsScreen(
    viewModel: FidelyBarViewModel,
    onBack: () -> Unit,
    inSheet: Boolean = false,
    onShowTutorial: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val store = remember { CardFileStore(context) }
    val themeMode by AppSettings.themeMode.collectAsState()
    val fontScale by AppSettings.fontScale.collectAsState()
    val accentHex by AppSettings.accentHex.collectAsState()
    val statusBarTint by AppSettings.statusBarTint.collectAsState()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    var pendingImport by remember { mutableStateOf<ImportOffer?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf<SettingsSection?>(null) }
    val scope = rememberCoroutineScope()

    // --- Stati per la dialog password dei backup criptati ---
    var passwordAction by remember { mutableStateOf<PasswordAction?>(null) }
    var pendingSavePassword by remember { mutableStateOf<String?>(null) }
    var pendingImportText by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordBusy by remember { mutableStateOf(false) }

    val versionInfo = remember {
        runCatching {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            "v${info.versionName} (build ${info.longVersionCode})"
        }.getOrDefault("versione non disponibile")
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) { pendingSavePassword = null; return@rememberLauncherForActivityResult }
        val password = pendingSavePassword
        pendingSavePassword = null
        val json = viewModel.exportJson()
        if (json != null && password != null) {
            scope.launch {
                val armored = withContext(Dispatchers.IO) { CardCrypto.encrypt(json, password) }
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(armored.toByteArray(Charsets.UTF_8)) } != null
                }.getOrDefault(false)
                Toast.makeText(context, if (ok) "Backup criptato salvato nel file" else "Impossibile salvare il file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        when {
            text.isNullOrBlank() ->
                Toast.makeText(context, "Backup non leggibile", Toast.LENGTH_SHORT).show()
            CardCrypto.isEncrypted(text) -> {
                pendingImportText = text
                passwordError = null
                passwordAction = PasswordAction.IMPORT
            }
            else -> pendingImport = ImportOffer(store.decode(text))
        }
    }

    // Il back di sistema da un sottomenù torna prima al menù principale delle
    // Impostazioni; solo dal menù principale chiude la finestra.
    BackHandler(enabled = section != null) {
        section = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FidelyBackgroundBrush())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .let { if (inSheet) it else it.navigationBarsPadding() },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                HeaderRow(
                    title = section?.title ?: "Impostazioni",
                    inSheet = inSheet,
                    onBack = { if (section != null) section = null else onBack() }
                )
            }

            val current = section
            item {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = {
                        val soft: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 900f)
                        val slide: FiniteAnimationSpec<IntOffset> = spring(dampingRatio = 0.8f, stiffness = 900f)
                        if (targetState != null && initialState == null) {
                            (fadeIn(animationSpec = soft) +
                                slideInHorizontally(animationSpec = slide) { it / 6 })
                                .togetherWith(fadeOut(animationSpec = soft))
                        } else {
                            fadeIn(animationSpec = soft)
                                .togetherWith(
                                    fadeOut(animationSpec = soft) +
                                        slideOutHorizontally(animationSpec = slide) { -it / 6 }
                                )
                        }
                    },
                    label = "settingsSection"
                ) { target ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (target == null) {
                            SectionCard(modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp)) {
                                Column {
                                    SettingsSectionRow(SettingsSection.APPEARANCE) { section = it }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    SettingsSectionRow(SettingsSection.BACKUP) { section = it }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    SettingsSectionRow(SettingsSection.INFO) { section = it }
                                }
                            }
                        } else {
                            when (target) {
                                SettingsSection.APPEARANCE -> {
                                    SectionTitle(
                                        title = "Aspetto",
                                        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                                    )
                                    SectionCard(modifier = Modifier.padding(start = 20.dp, end = 20.dp)) {
                                        AppearanceContent(
                                            themeMode = themeMode,
                                            onThemeChange = AppSettings::setThemeMode,
                                            fontScale = fontScale,
                                            onFontScaleChange = AppSettings::setFontScale,
                                            accentHex = accentHex,
                                            onAccentChange = AppSettings::setAccentHex,
                                            statusBarTint = statusBarTint,
                                            onStatusBarTintChange = AppSettings::setStatusBarTint
                                        )
                                    }
                                }
                                SettingsSection.BACKUP -> {
                                    SectionTitle(
                                        title = "Backup e ripristino",
                                        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                                    )
                                    SectionCard(modifier = Modifier.padding(start = 20.dp, end = 20.dp)) {
                                         BackupContent(
                                             cards = cards,
                                             onExport = {
                                                 if (cards.isNotEmpty()) {
                                                     passwordError = null
                                                     passwordAction = PasswordAction.EXPORT_SHARE
                                                 }
                                             },
                                             onSave = {
                                                 if (cards.isNotEmpty()) {
                                                     passwordError = null
                                                     passwordAction = PasswordAction.EXPORT_SAVE
                                                 }
                                             },
                                             onImport = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                                             onDeleteAll = { showDeleteConfirm = true }
                                         )
                                    }
                                }
                                 SettingsSection.INFO -> {
                                     SectionTitle(
                                         title = "Info su FidelyBar",
                                         modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                                     )
                                     SectionCard(modifier = Modifier.padding(start = 20.dp, end = 20.dp)) {
                                         InfoContent(versionInfo, onShowTutorial = onShowTutorial)
                                     }
                                 }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingImport?.let { offer ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Ripristina backup") },
            text = {
                Text(
                    if (offer.cards.isEmpty())
                        "Il file non contiene carte. Le carte attuali non verranno modificate."
                    else
                        "Importare ${offer.cards.size} ${if (offer.cards.size == 1) "carta" else "carte"}? Le carte attuali verranno sostituite."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImport = null
                        if (offer.cards.isNotEmpty()) {
                            viewModel.importCards(offer.cards)
                            Toast.makeText(
                                context,
                                "Backup ripristinato: ${offer.cards.size} carte",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) { Text("Ripristina") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("Annulla") }
            }
        )
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Elimina tutte le carte") },
            text = {
                Text("Vuoi davvero eliminare tutte le ${cards.size} carte dal dispositivo? L'azione è irreversibile.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAllCards()
                        Toast.makeText(context, "Tutte le carte sono state eliminate", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Elimina", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annulla") }
            }
        )
    }

    // --- Dialog password per esportazione e importazione ---
    passwordAction?.let { action ->
        var passwordInput by remember(passwordAction) { mutableStateOf("") }
        val (title, description, confirmLabel) = when (action) {
            PasswordAction.EXPORT_SAVE -> Triple(
                "Proteggi il backup",
                "Scegli una password (min. 4 caratteri). Il file salvato sarà cifrato.",
                "Salva file"
            )
            PasswordAction.EXPORT_SHARE -> Triple(
                "Proteggi il backup",
                "Scegli una password (min. 4 caratteri). Il backup condiviso sarà sicuro.",
                "Crea e condividi"
            )
            PasswordAction.IMPORT -> Triple(
                "Password del backup",
                "Inserisci la password impostata alla creazione di questo file di backup.",
                "Ripristina"
            )
        }
        AlertDialog(
            onDismissRequest = { if (!passwordBusy) { passwordAction = null; passwordError = null; pendingImportText = null } },
            title = { Text(title) },
            text = {
                Column {
                    Text(description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !passwordBusy
                    )
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (passwordBusy) {
                        Spacer(Modifier.height(8.dp))
                        Text("Elaborazione in corso...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = passwordInput.length >= 4 && !passwordBusy,
                    onClick = {
                        when (action) {
                            PasswordAction.EXPORT_SAVE -> {
                                passwordAction = null
                                passwordError = null
                                pendingSavePassword = passwordInput
                                saveFileLauncher.launch("fidelybar_backup.fid")
                            }
                            PasswordAction.EXPORT_SHARE -> {
                                passwordAction = null
                                passwordError = null
                                val json = viewModel.exportJson()
                                if (json != null) {
                                    scope.launch {
                                        val armored = withContext(Dispatchers.IO) { CardCrypto.encrypt(json, passwordInput) }
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "FidelyBar backup protetto")
                                            putExtra(Intent.EXTRA_TEXT, armored)
                                        }
                                        context.startActivity(Intent.createChooser(send, "Esporta backup FidelyBar"))
                                    }
                                }
                            }
                            PasswordAction.IMPORT -> {
                                val text = pendingImportText
                                if (text != null) {
                                    passwordBusy = true
                                    passwordError = null
                                    scope.launch {
                                        val json = withContext(Dispatchers.IO) { CardCrypto.decrypt(text, passwordInput) }
                                        if (json == null) {
                                            passwordBusy = false
                                            passwordError = "Password errata o file non valido"
                                        } else {
                                            passwordBusy = false
                                            passwordAction = null
                                            pendingImportText = null
                                            pendingImport = ImportOffer(store.decode(json))
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { Text(confirmLabel) }
            },
            dismissButton = {
                TextButton(
                    enabled = !passwordBusy,
                    onClick = { passwordAction = null; passwordError = null; pendingImportText = null }
                ) { Text("Annulla") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSegmentedRow(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, Icons.Filled.BrightnessAuto, "Sistema"),
        Triple(ThemeMode.LIGHT, Icons.Filled.LightMode, "Chiaro"),
        Triple(ThemeMode.DARK, Icons.Filled.DarkMode, "Scuro")
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, icon, label) ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontScaleSegmentedRow(
    selected: FontScale,
    onSelect: (FontScale) -> Unit
) {
    val options = listOf(
        FontScale.NORMAL to "Normale",
        FontScale.LARGE to "Grande",
        FontScale.EXTRA_LARGE to "Extra"
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (scale, label) ->
            SegmentedButton(
                selected = selected == scale,
                onClick = { onSelect(scale) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun HeaderRow(title: String, inSheet: Boolean, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (inSheet) it else it.statusBarsPadding() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SettingsSectionRow(
    section: SettingsSection,
    onClick: (SettingsSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(section) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = section.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceContent(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    fontScale: FontScale,
    onFontScaleChange: (FontScale) -> Unit,
    accentHex: String?,
    onAccentChange: (String?) -> Unit,
    statusBarTint: Boolean,
    onStatusBarTintChange: (Boolean) -> Unit
) {
    Column {
        CardVisual(
            card = UserCard(
                id = "settings-preview",
                title = "La tua carta",
                number = "0000 0000 0000 0000",
                format = BarcodeFormatType.EAN13,
                primaryColorHex = accentHex ?: "#0E7C61",
                secondaryColorHex = accentHex?.let {
                    val r = it.removePrefix("#").toLong(16)
                    val ri = ((r shr 16 and 0xFF) * 0.72f).toInt().coerceIn(0, 255)
                    val gi = ((r shr 8 and 0xFF) * 0.72f).toInt().coerceIn(0, 255)
                    val bi = ((r and 0xFF) * 0.72f).toInt().coerceIn(0, 255)
                    "#%06X".format((ri shl 16) or (gi shl 8) or bi)
                } ?: "#0A5C49",
                monogram = 'F',
                logoBorderWhite = false,
                logoBorderSize = 1
            ),
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        Text(
            text = "Tema",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        ThemeSegmentedRow(selected = themeMode, onSelect = onThemeChange)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Dimensione carattere",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        FontScaleSegmentedRow(selected = fontScale, onSelect = onFontScaleChange)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Colore accento",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        AccentSelector(selected = accentHex, onSelect = onAccentChange)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Usato per lo sfondo delle schermate e il pulsante Nuova carta. In tema chiaro diventa una tinta tenue, in tema scuro una tinta profonda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Barre di sistema",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        SettingsSwitchRow(
            title = "Colora la barra di sistema",
            subtitle = "Estende il colore del tema alla barra in alto",
            checked = statusBarTint,
            onCheckedChange = onStatusBarTintChange
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "La barra in basso resta trasparente e si fonde con lo sfondo. Le icone delle barre restano sempre ben visibili sia in tema chiaro che scuro.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Tema, accento e barre si applicano subito a tutta l'app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Anteprima",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        CardPreview()
    }
}

private val accentChoices = listOf(
    "#006C4C" to "Verde",
    "#0E7C61" to "Smeraldo",
    "#0891B2" to "Ciano",
    "#3B82F6" to "Blu",
    "#5B5BD6" to "Indaco",
    "#7C3AED" to "Viola",
    "#DB2777" to "Rosa",
    "#DC2626" to "Rosso",
    "#EA580C" to "Arancione",
    "#EAB308" to "Oro",
    "#64748B" to "Ardesia",
    "#0F172A" to "Notte"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentSelector(selected: String?, onSelect: (String?) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccentDot(
            selected = selected == null,
            color = MaterialTheme.colorScheme.primaryContainer,
            label = "Auto",
            onClick = { onSelect(null) }
        )
        accentChoices.forEach { (hex, label) ->
            val color = rememberColor(hex)
            AccentDot(
                selected = selected == hex,
                color = color,
                label = label,
                onClick = { onSelect(hex) }
            )
        }
    }
}

@Composable
private fun AccentDot(
    selected: Boolean,
    color: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = label,
                    tint = foregroundFor(color),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BackupContent(
    cards: List<UserCard>,
    onExport: () -> Unit,
    onSave: () -> Unit,
    onImport: () -> Unit,
    onDeleteAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Le carte sono salvate solo sul dispositivo. Crea un backup per non perderle o spostarle su un altro telefono.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = when {
                        cards.isEmpty() -> "Nessuna carta sul dispositivo"
                        cards.size == 1 -> "1 carta sul dispositivo"
                        else -> "${cards.size} carte sul dispositivo"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        BackupActionLabel("Crea un backup")
        BackupActionButton(
            icon = Icons.Filled.Save,
            label = "Salva su file",
            subtitle = "Scegli dove salvare il file .json",
            onClick = onSave,
            enabled = cards.isNotEmpty()
        )
        BackupActionButton(
            icon = Icons.Filled.Share,
            label = "Esporta e condividi",
            subtitle = "Invia il backup con un'altra app",
            onClick = onExport,
            enabled = cards.isNotEmpty(),
            prominent = true
        )
        Spacer(Modifier.height(4.dp))
        BackupActionLabel("Ripristina")
        BackupActionButton(
            icon = Icons.Filled.Restore,
            label = "Ripristina da un file",
            subtitle = "Le carte attuali verranno sostituite",
            onClick = onImport
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        TextButton(
            onClick = onDeleteAll,
            enabled = cards.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Elimina tutte le carte",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BackupActionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun BackupActionButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    prominent: Boolean = false
) {
    val container: @Composable (Modifier) -> Unit = { mod ->
        if (prominent) {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(18.dp),
                modifier = mod
            ) { BackupActionButtonContent(icon, label, subtitle, onPrimary = true) }
        } else {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(18.dp),
                modifier = mod
            ) { BackupActionButtonContent(icon, label, subtitle, onPrimary = false) }
        }
    }
    container(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

@Composable
private fun BackupActionButtonContent(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onPrimary: Boolean
) {
    val subtitleColor =
        if (onPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
        }
    }
}

@Composable
private fun InfoContent(versionInfo: String, onShowTutorial: (() -> Unit)?) {
    Column {
        SubSectionLabel("Tutorial")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onShowTutorial?.invoke() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Rivedi il tutorial iniziale",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mostra nuovamente le schermate informative e di benvenuto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SubSectionLabel("Versione")
        SettingsItemRow(
            SettingsItem(
                icon = Icons.Filled.Android,
                title = "FidelyBar",
                subtitle = versionInfo
            )
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SettingsItemRow(
            SettingsItem(
                icon = Icons.Filled.Info,
                title = "Sistema",
                subtitle = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) su ${Build.MANUFACTURER}"
            )
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SubSectionLabel("L'app")
        SettingsItemRow(
            SettingsItem(
                icon = Icons.Filled.PhotoCamera,
                title = "Cosa fa FidelyBar",
                subtitle = "Le tue carte fedeltà, sempre con te: anche offline, un tap e sei alla cassa."
            )
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SubSectionLabel("Privacy e dati")
        privacyItems.forEach { item ->
            SettingsItemRow(item)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SubSectionLabel("Loghi e attribuzioni")
        SettingsItemRow(
            SettingsItem(
                icon = Icons.Filled.PhotoLibrary,
                title = "Loghi delle catene",
                subtitle = "Fonti: Wikipedia e Wikimedia Commons, licenza CC BY-SA 4.0. I marchi appartengono ai rispettivi detentori."
            )
        )
    }
}

@Composable
private fun CardPreview() {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = accentTone(primary, if (LocalDarkTheme.current) 0.45f else 0.55f)
    val sample = remember(primary, secondary) {
        UserCard(
            id = "preview",
            title = "Carta di esempio",
            number = "8001234567890",
            format = BarcodeFormatType.EAN13,
            primaryColorHex = colorToHex(primary),
            secondaryColorHex = colorToHex(secondary),
            monogram = 'Φ'
        )
    }
    CardVisual(
        card = sample,
        modifier = Modifier.fillMaxWidth(),
        compact = true,
        onClick = {}
    )
}

private fun colorToHex(color: Color): String =
    "#%06X".format(0xFFFFFF and color.toArgb())

private val privacyItems = listOf(
    SettingsItem(
        icon = Icons.Filled.Lock,
        title = "Offline e senza account",
        subtitle = "Nessun account, nessuna registrazione, nessun dato inviato in rete."
    ),
    SettingsItem(
        icon = Icons.Filled.Info,
        title = "Dati solo sul dispositivo",
        subtitle = "Le carte restano nel tuo telefono, salvate in locale. Disinstalla e i dati spariscono."
    ),
    SettingsItem(
        icon = Icons.Filled.Info,
        title = "Nessuna pubblicità né telemetria",
        subtitle = "FidelyBar non traccia i tuoi comportamenti, non mostra annunci e non raccoglie statistiche."
    )
)

@Composable
private fun SubSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}