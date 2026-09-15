package com.card.fidelybar.ui.onboarding

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.card.fidelybar.data.AppSettings
import com.card.fidelybar.data.ThemeMode
import com.card.fidelybar.ui.components.foregroundFor
import com.card.fidelybar.ui.components.rememberColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDialog(
    onFinished: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val themeMode by AppSettings.themeMode.collectAsState()
    val accentHex by AppSettings.accentHex.collectAsState()

    val accentChoices = listOf(
        null to "Auto",
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

    AlertDialog(
        onDismissRequest = {},
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (page) {
                        0 -> "Nota importante sulla privacy"
                        1 -> "Benvenuto in FidelyBar"
                        2 -> "Carta Personalizzata"
                        else -> "Personalizza l'aspetto"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${page + 1}/4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                when (page) {
                    0 -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Avviso di sicurezza",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "FidelyBar è un'applicazione progettata esclusivamente per memorizzare carte fedeltà e codici a barre commerciali.\n\n" +
                                            "NON inserire in alcun modo dati personali sensibili, codici fiscali, IBAN, carte di credito, password o documenti di identità.\n\n" +
                                            "Lo sviluppatore declina ogni responsabilità per un uso improprio dell'applicazione o per l'inserimento di dati diversi dalle normali carte fedeltà.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    1 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "Le tue carte fedeltà, sempre con te",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "FidelyBar raccoglie tutte le tue tessere fedeltà in un unico portafoglio digitale. Funziona completamente offline, senza bisogno di account, e ti permette di mostrare i codici a barre alla cassa con un solo tap.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    2 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "La carta \"Personalizzata\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Non trovi il negozio che cerchi nella lista predefinita? Nessun problema! Usa la carta \"Personalizzata\" per aggiungere qualsiasi negozio mancante, scegliendo nome, colori e formato del codice a barre in pochi secondi.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Scegli il tema",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val options = listOf(
                                Triple(ThemeMode.SYSTEM, Icons.Filled.BrightnessAuto, "Sistema"),
                                Triple(ThemeMode.LIGHT, Icons.Filled.LightMode, "Chiaro"),
                                Triple(ThemeMode.DARK, Icons.Filled.DarkMode, "Scuro")
                            )
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                options.forEachIndexed { index, (mode, icon, label) ->
                                    SegmentedButton(
                                        selected = themeMode == mode,
                                        onClick = { AppSettings.setThemeMode(mode) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Colore accento iniziale",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accentChoices.forEach { (hex, label) ->
                                    val isSelected = accentHex == hex
                                    val boxColor = if (hex == null) MaterialTheme.colorScheme.primaryContainer else rememberColor(hex)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(boxColor)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { AppSettings.setAccentHex(hex) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = label,
                                                tint = foregroundFor(boxColor),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                // Dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == page) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == page) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (page < 3) {
                        page++
                    } else {
                        AppSettings.setHasSeenOnboarding(true)
                        onFinished()
                    }
                }
            ) {
                Text(if (page < 3) "Avanti" else "Fine")
            }
        },
        dismissButton = {
            if (page > 0) {
                TextButton(onClick = { page-- }) {
                    Text("Indietro")
                }
            }
        }
    )
}
