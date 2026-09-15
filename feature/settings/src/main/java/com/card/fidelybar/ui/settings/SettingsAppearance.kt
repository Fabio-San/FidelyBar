package com.card.fidelybar.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.FontScale
import com.card.fidelybar.data.ThemeMode
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.ColorChoice
import com.card.fidelybar.ui.components.ColorShadeDialog
import com.card.fidelybar.ui.components.Swatch
import com.card.fidelybar.ui.components.basePalette
import com.card.fidelybar.ui.components.customPalette
import com.card.fidelybar.ui.theme.LocalDarkTheme
import com.card.fidelybar.ui.theme.accentTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeSegmentedRow(
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
internal fun FontScaleSegmentedRow(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppearanceContent(
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
                primaryColorHex = accentHex ?: "#64748B",
                secondaryColorHex = accentHex?.let {
                    val r = it.removePrefix("#").toLong(16)
                    val ri = ((r shr 16 and 0xFF) * 0.72f).toInt().coerceIn(0, 255)
                    val gi = ((r shr 8 and 0xFF) * 0.72f).toInt().coerceIn(0, 255)
                    val bi = ((r and 0xFF) * 0.72f).toInt().coerceIn(0, 255)
                    "#%06X".format((ri shl 16) or (gi shl 8) or bi)
                } ?: "#475569",
                monogram = 'F',
                logoBorderWhite = false,
                logoBorderSize = 1
            ),
            onClick = {},
            compact = true,
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
            text = "Colore",
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
            text = "Tema, accento e barre si applicano subito a tutta l'app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccentSelector(selected: String?, onSelect: (String?) -> Unit) {
    var shadeBase by remember { mutableStateOf<ColorChoice?>(null) }
    var expanded by remember { mutableStateOf(false) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        basePalette.forEach { choice ->
            val isSel = selected == choice.primary
            Swatch(
                choice = choice,
                selected = isSel,
                onClick = { shadeBase = choice }
            )
        }
    }

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(spring(dampingRatio = 0.9f, stiffness = 500f)) + fadeIn(),
        exit = shrinkVertically(spring(dampingRatio = 0.9f, stiffness = 500f)) + fadeOut()
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            customPalette.filterNot { base -> basePalette.any { it.primary == base.primary } }.forEach { choice ->
                val isSel = selected == choice.primary
                Swatch(
                    choice = choice,
                    selected = isSel,
                    onClick = { shadeBase = choice }
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (expanded) "Meno colori" else "Tutti i colori",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    shadeBase?.let { base ->
        ColorShadeDialog(
            base = base,
            preview = { tonePrimary, toneSecondary ->
                CardPreview(primaryHex = tonePrimary, secondaryHex = toneSecondary)
            },
            onConfirm = { choice ->
                onSelect(choice.primary)
                shadeBase = null
            },
            onDismiss = { shadeBase = null }
        )
    }
}

@Composable
internal fun SettingsSwitchRow(
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
internal fun CardPreview(primaryHex: String? = null, secondaryHex: String? = null) {
    val primary = MaterialTheme.colorScheme.primary
    val themeSecondary = accentTone(primary, if (LocalDarkTheme.current) 0.45f else 0.55f)
    val sample = remember(primaryHex, secondaryHex, primary, themeSecondary) {
        UserCard(
            id = "preview",
            title = "Carta di esempio",
            number = "8001234567890",
            format = BarcodeFormatType.EAN13,
            primaryColorHex = primaryHex ?: colorToHex(primary),
            secondaryColorHex = secondaryHex ?: colorToHex(themeSecondary),
            monogram = '\u03A6'
        )
    }
    CardVisual(
        card = sample,
        modifier = Modifier.fillMaxWidth(),
        compact = true,
        onClick = {}
    )
}

internal fun colorToHex(color: Color): String =
    "#%06X".format(0xFFFFFF and color.toArgb())
