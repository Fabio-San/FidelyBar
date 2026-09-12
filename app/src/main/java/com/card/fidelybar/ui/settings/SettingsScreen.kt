package com.card.fidelybar.ui.settings

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.CardFileStore
import com.card.fidelybar.data.FontScale
import com.card.fidelybar.data.ThemeMode
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private data class ImportOffer(val cards: List<UserCard>)

@Composable
fun SettingsScreen(viewModel: FidelyBarViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { CardFileStore(context) }
    val themeMode by AppSettings.themeMode.collectAsState()
    val fontScale by AppSettings.fontScale.collectAsState()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    var pendingImport by remember { mutableStateOf<ImportOffer?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
        if (uri == null) return@rememberLauncherForActivityResult
        val json = viewModel.exportJson()
        if (json != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } != null
            }.getOrDefault(false)
            Toast.makeText(
                context,
                if (ok) "Backup salvato nel file" else "Impossibile salvare il file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
        }.getOrNull()
        if (text.isNullOrBlank()) {
            Toast.makeText(context, "Backup non leggibile", Toast.LENGTH_SHORT).show()
        } else {
            pendingImport = ImportOffer(store.decode(text))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Impostazioni",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Aspetto",
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                )
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column {
                        Text(
                            text = "Tema",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        ThemeSegmentedRow(selected = themeMode, onSelect = AppSettings::setThemeMode)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Dimensione carattere",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        FontScaleSegmentedRow(selected = fontScale, onSelect = AppSettings::setFontScale)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Anteprima",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        CardPreview()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Colori dinamici e dimensione carattere si applicano subito a tutta l'app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Backup",
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Le carte sono salvate solo sul dispositivo. Esporta un backup per conservarlo o passarlo a un altro telefono, oppure ripristina un file esportato in precedenza.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (cards.isEmpty())
                                "Nessuna carta salvata sul dispositivo."
                            else
                                "${cards.size} ${if (cards.size == 1) "carta salvata" else "carte salvate"} sul dispositivo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.exportJson()?.let { json ->
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_SUBJECT, "FidelyBar backup")
                                            putExtra(Intent.EXTRA_TEXT, json)
                                        }
                                        context.startActivity(Intent.createChooser(send, "Esporta backup FidelyBar"))
                                    }
                                },
                                enabled = cards.isNotEmpty(),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Backup,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Esporta")
                            }
                            OutlinedButton(
                                onClick = {
                                    saveFileLauncher.launch("fidelybar_backup.json")
                                },
                                enabled = cards.isNotEmpty(),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Salva")
                            }
                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                },
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Restore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Importa")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            enabled = cards.isNotEmpty(),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Backup,
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
            }

            item {
                SectionTitle(
                    title = "Versione",
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column {
                        SettingsItemRow(
                            SettingsItem(
                                icon = Icons.Filled.Android,
                                title = "FidelyBar",
                                subtitle = versionInfo
                            )
                        )
                        SettingsItemRow(
                            SettingsItem(
                                icon = Icons.Filled.Info,
                                title = "Sistema",
                                subtitle = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) su ${Build.MANUFACTURER}"
                            )
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Info",
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column {
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
            }
        }
    }

    pendingImport?.let { offer ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Importa backup") },
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
                                "Backup importato: ${offer.cards.size} carte",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) { Text("Importa") }
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
private fun CardPreview() {
    val sample = remember {
        UserCard(
            id = "preview",
            title = "Carta di esempio",
            number = "8001234567890",
            format = BarcodeFormatType.EAN13,
            primaryColorHex = "#006C4C",
            secondaryColorHex = "#004D35",
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
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
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