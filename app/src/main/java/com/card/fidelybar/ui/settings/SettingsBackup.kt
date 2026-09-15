package com.card.fidelybar.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.card.fidelybar.data.UserCard

internal data class ImportOffer(val cards: List<UserCard>)

internal enum class PasswordAction { EXPORT_SHARE, EXPORT_SAVE, IMPORT }

@Composable
internal fun BackupContent(
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
internal fun BackupActionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
internal fun BackupActionButton(
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
internal fun BackupActionButtonContent(
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
