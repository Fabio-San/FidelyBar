package com.card.fidelybar.ui.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import androidx.core.graphics.toColorInt
import com.card.fidelybar.data.StoreCatalog
import com.card.fidelybar.data.StorePreset
import com.card.fidelybar.ui.components.LogoOrMonogram

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StorePickerSection(
    storeChosen: Boolean,
    isCustom: Boolean,
    monogram: Char,
    logoUrl: String?,
    name: String,
    subtitle: String,
    primaryHex: String,
    storeId: String?,
    customPalettePrimary: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onSelectPreset: (StorePreset) -> Unit,
    onSelectCustom: () -> Unit,
    onChange: () -> Unit
) {
    AnimatedContent(
        targetState = storeChosen,
        transitionSpec = {
            (fadeIn(tween(200)) + slideInHorizontally(tween(240)) { it / 8 })
                .togetherWith(
                    fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { -it / 8 }
                )
        },
        label = "storePicker"
    ) { chosen ->
        if (chosen) {
            WizardStepScroll {
                SelectedStoreBar(
                    monogram = monogram,
                    logoUrl = logoUrl,
                    name = name,
                    subtitle = subtitle,
                    primaryHex = primaryHex,
                    isCustom = isCustom,
                    storeId = storeId,
                    onChange = onChange
                )
            }
        } else {
            WizardStepScroll {
                Box(
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp).fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Cerca tra ${StoreCatalog.all.size} locali...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { onFocusChanged(it.isFocused) }
                    )
                }
                val q = query.trim().lowercase()
                val featuredIds = listOf("conad", "coop", "eurospin", "trony", "crai", "md", "cfadda")
                if (q.isEmpty()) {
                    Text(
                        text = "Più richieste",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (q.isEmpty() || "personalizzata".contains(q) || "su misura".contains(q)) {
                        StoreChip(
                            label = "Personalizzata",
                            monogram = '\u03A6',
                            primaryHex = customPalettePrimary,
                            selected = false,
                            onClick = onSelectCustom
                        )
                    }
                    StoreCatalog.all.forEach { preset ->
                        val matches = if (q.isEmpty()) {
                            preset.id in featuredIds
                        } else {
                            preset.name.lowercase().contains(q) ||
                                preset.searchAliases.any { it.lowercase().contains(q) }
                        }
                        if (matches) {
                            StoreChip(
                                label = preset.name,
                                monogram = preset.monogram,
                                primaryHex = preset.primaryColorHex,
                                selected = false,
                                logoKey = preset.id,
                                onClick = { onSelectPreset(preset) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SelectedStoreBar(
    monogram: Char,
    logoUrl: String?,
    name: String,
    subtitle: String,
    primaryHex: String,
    isCustom: Boolean,
    storeId: String?,
    onChange: () -> Unit
) {
    val color = Color(primaryHex.toColorInt())
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LogoOrMonogram(
                    monogram = monogram,
                    logoUrl = logoUrl,
                    containerColor = if (isCustom) MaterialTheme.colorScheme.primary else color,
                    contentColor = Color.White,
                    size = 34,
                    logoKey = storeId
                )
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        TextButton(onClick = onChange) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Cambia")
        }
    }
}

@Composable
private fun StoreChip(
    label: String,
    monogram: Char,
    primaryHex: String,
    selected: Boolean,
    logoKey: String? = null,
    onClick: () -> Unit
) {
    val color = Color(primaryHex.toColorInt())
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogoOrMonogram(
                monogram = monogram,
                logoUrl = null,
                containerColor = color.copy(alpha = 0.9f),
                contentColor = Color.White,
                size = 28,
                logoKey = logoKey
            )
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
