package com.card.fidelybar.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class StepState { DONE, ACTIVE, TODO }

@Composable
internal fun WizardStepper(current: Int, onNavigate: (Int) -> Unit) {
    val labels = listOf("Negozio", "Codice", "Aspetto")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { i, label ->
            val state = when {
                current > i -> StepState.DONE
                current == i -> StepState.ACTIVE
                else -> StepState.TODO
            }
            StepDot(
                index = i,
                label = label,
                state = state,
                onClick = if (state == StepState.DONE) ({ onNavigate(i) }) else null
            )
            if (i < labels.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (current > i) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun StepDot(index: Int, label: String, state: StepState, onClick: (() -> Unit)?) {
    val bg = when (state) {
        StepState.ACTIVE -> MaterialTheme.colorScheme.primary
        StepState.DONE -> MaterialTheme.colorScheme.primaryContainer
        StepState.TODO -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when (state) {
        StepState.ACTIVE -> MaterialTheme.colorScheme.onPrimary
        StepState.DONE -> MaterialTheme.colorScheme.onPrimaryContainer
        StepState.TODO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .let { if (onClick != null) it.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick) else it }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            if (state == StepState.DONE) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
            } else {
                Text(
                    (index + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (state == StepState.ACTIVE) FontWeight.Bold else FontWeight.Normal,
            color = if (state == StepState.TODO) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun WizardStepScroll(
    scrollState: ScrollState = rememberScrollState(),
    bottomSpacer: Dp = 110.dp,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
    ) {
        content()
        // Spazio protettivo in fondo per evitare che i pulsanti coprano il contenuto,
        // aggiunto come elemento della colonna così influisce sullo scroll solo se necessario.
        Spacer(Modifier.height(bottomSpacer))
    }
}

@Composable
internal fun WizardActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.height(54.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
