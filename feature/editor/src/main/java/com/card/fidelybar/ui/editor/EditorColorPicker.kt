package com.card.fidelybar.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.components.CardVisual
import com.card.fidelybar.ui.components.ColorChoice
import com.card.fidelybar.ui.components.ColorShadeDialog
import com.card.fidelybar.ui.components.LogoOrMonogram
import com.card.fidelybar.ui.components.LogoPalette
import com.card.fidelybar.ui.components.LogoColors
import com.card.fidelybar.ui.components.Swatch
import com.card.fidelybar.ui.components.buildLogoChoices
import com.card.fidelybar.ui.components.customPalette

@Composable
internal fun ColorPickerTile(
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
internal fun ColorPickerScreen(
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                logoChoices.forEach { choice ->
                                    val isRecommended = choice == recommendedChoice
                                    Column {
                                        Swatch(
                                            choice = choice,
                                            selected = shadeBase?.primary == choice.primary,
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            customPalette.forEach { choice ->
                                Swatch(
                                    choice = choice,
                                    selected = shadeBase?.primary == choice.primary,
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
            preview = { tonePrimary, toneSecondary ->
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
            },
            onConfirm = { choice ->
                onSelect(choice)
                shadeBase = null
            },
            onDismiss = { shadeBase = null }
        )
    }
}
