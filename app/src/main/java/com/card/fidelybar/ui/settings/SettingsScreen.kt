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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.CardCrypto
import com.card.fidelybar.data.CardRepositoryImpl
import com.card.fidelybar.ui.theme.FidelyBackgroundBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val store = remember { CardRepositoryImpl.create(context) }
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
        AlertDialog(
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
        AlertDialog(
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