package com.card.fidelybar.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.card.fidelybar.barcode.BarcodeEngine
import com.card.fidelybar.data.BarcodeFormatType
import com.card.fidelybar.data.UserCard
import com.card.fidelybar.ui.theme.Ink

@Composable
fun CardCodeView(
    card: UserCard,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 18
) {
    BoxWithConstraints(modifier = modifier) {
        val isSquare = card.format == BarcodeFormatType.QR_CODE ||
            card.format == BarcodeFormatType.DATA_MATRIX
        val shape = RoundedCornerShape(cornerRadius.dp)
        val widthPx = if (constraints.maxWidth != androidx.compose.ui.unit.Constraints.Infinity) constraints.maxWidth else 800
        val heightPx = (widthPx * if (isSquare) 1.05f else 0.42f).toInt()
        val bitmap = remember(card.id, card.number, card.format, widthPx, heightPx) {
            runCatching {
                BarcodeEngine.generate(card.number, card.format, widthPx, heightPx)
            }.getOrNull()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.White, shape)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Contenuto non valido per il formato ${card.format.label}",
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}