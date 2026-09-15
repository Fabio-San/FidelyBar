package com.card.fidelybar.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
internal fun InfoContent(versionInfo: String, onShowTutorial: (() -> Unit)?) {
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

internal val privacyItems = listOf(
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
internal fun SubSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
internal fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
internal fun SectionCard(
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
internal fun SettingsItemRow(item: SettingsItem) {
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
