package com.card.fidelybar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import kotlin.math.abs
import kotlin.math.roundToInt

data class ColorChoice(val primary: String, val secondary: String, val label: String)

val customDefaultPalette = ColorChoice("#6B7280", "#4B5563", "Grigio")

val customPalette = listOf(
    ColorChoice("#EF4444", "#DC2626", "Rosso"),
    ColorChoice("#F43F5E", "#E11D48", "Cremisi"),
    ColorChoice("#EC4899", "#DB2777", "Rosa"),
    ColorChoice("#DB2777", "#BE185D", "Fucsia"),
    ColorChoice("#F97316", "#EA580C", "Arancione"),
    ColorChoice("#EAB308", "#CA8A04", "Oro"),
    ColorChoice("#F59E0B", "#D97706", "Ambra"),
    ColorChoice("#84CC16", "#65A30D", "Lime"),
    ColorChoice("#0E7C61", "#0A5C49", "Verde"),
    ColorChoice("#10B981", "#059669", "Smeraldo"),
    ColorChoice("#0891B2", "#0E7490", "Ciano"),
    ColorChoice("#3B82F6", "#2563EB", "Blu"),
    ColorChoice("#6366F1", "#4648D8", "Iris"),
    ColorChoice("#5B5BD6", "#3D3DA8", "Indaco"),
    ColorChoice("#8B5CF6", "#6D28D9", "Viola"),
    ColorChoice("#A855F7", "#9333EA", "Viola chiaro"),
    ColorChoice("#4C1D95", "#3B0A72", "Borgogna"),
    ColorChoice("#E05A47", "#B84332", "Corallo"),
    ColorChoice("#7C2D12", "#5B1F0A", "Cioccolato"),
    ColorChoice("#14532D", "#0E3A20", "Foresta"),
    ColorChoice("#0C4A6E", "#082F49", "Oceano"),
    ColorChoice("#64748B", "#475569", "Ardesia"),
    ColorChoice("#78716C", "#57534E", "Pietra"),
    ColorChoice("#111827", "#030712", "Notte")
)

val basePalette = listOf(
    ColorChoice("#EF4444", "#DC2626", "Rosso"),
    ColorChoice("#EAB308", "#CA8A04", "Giallo"),
    ColorChoice("#0E7C61", "#0A5C49", "Verde"),
    ColorChoice("#10B981", "#059669", "Smeraldo"),
    ColorChoice("#3B82F6", "#2563EB", "Blu"),
    ColorChoice("#0891B2", "#0E7490", "Ciano"),
    ColorChoice("#111827", "#030712", "Nero"),
    ColorChoice("#64748B", "#475569", "Ardesia"),
    ColorChoice("#8B5CF6", "#6D28D9", "Viola"),
    ColorChoice("#DB2777", "#BE185D", "Fucsia"),
    ColorChoice("#F97316", "#EA580C", "Arancione"),
    ColorChoice("#EC4899", "#DB2777", "Rosa")
)

fun hexColor(color: Int) = "#%06X".format(color and 0x00FFFFFF)

val shadeFactors = listOf(0.42f, 0.58f, 0.74f, 0.88f, 1f, 1.14f, 1.32f)

fun gradientFor(base: Int, shade: Float, intensity: Float, darkToLight: Boolean): Pair<String, String> {
    val v = LogoPalette.scaled(base, shade)
    val primary = LogoPalette.scaled(v, if (darkToLight) intensity * 0.85f else intensity)
    val secondary = LogoPalette.scaled(v, if (darkToLight) intensity * 1.15f else intensity * 0.62f)
    return hexColor(primary) to hexColor(secondary)
}

fun buildLogoChoices(c: LogoColors): List<ColorChoice> {
    val p = c.primary
    return listOf(
        ColorChoice(hexColor(p), hexColor(c.secondary), "Dal logo"),
        ColorChoice(hexColor(c.recommendedPrimary), hexColor(c.recommendedSecondary), "Base")
    )
}

@Composable
fun Swatch(
    choice: ColorChoice,
    selected: Boolean,
    size: Int = 38,
    onClick: () -> Unit
) {
    val color = Color(choice.primary.toColorInt())
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
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
                    contentDescription = choice.label,
                    tint = foregroundFor(color),
                    modifier = Modifier.size((size * 0.42f).dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = choice.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorShadeDialog(
    base: ColorChoice,
    preview: @Composable (tonePrimary: String, toneSecondary: String) -> Unit,
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
                    preview(tonePrimary, toneSecondary)
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(variant))
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { shade = factor },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = foregroundFor(Color(variant)),
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
                                "Gradiente come i colori suggeriti",
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