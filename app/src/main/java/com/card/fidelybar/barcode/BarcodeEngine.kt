package com.card.fidelybar.barcode

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.card.fidelybar.data.BarcodeFormatType
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import kotlin.math.max

data class DecodeResult(
    val content: String,
    val format: BarcodeFormatType
)

object BarcodeEngine {

    /** Normalizza il contenuto per il formato richiesto (es. calcola la cifra di controllo EAN). */
    fun normalize(content: String, format: BarcodeFormatType): String {
        when (format) {
            BarcodeFormatType.EAN13 -> return normalizeEan(content, 13)
            BarcodeFormatType.EAN8 -> return normalizeEan(content, 8)
            BarcodeFormatType.UPC_A -> return normalizeUpc(content)
            else -> return content.trim()
        }
    }

    fun expectedDigits(format: BarcodeFormatType): Int? = when (format) {
        BarcodeFormatType.EAN13 -> 13
        BarcodeFormatType.EAN8 -> 8
        BarcodeFormatType.UPC_A -> 12
        else -> null
    }

    fun isMissingCheckDigit(content: String, format: BarcodeFormatType): Boolean = when (format) {
        BarcodeFormatType.EAN13 -> content.length == 12
        BarcodeFormatType.EAN8 -> content.length == 7
        BarcodeFormatType.UPC_A -> content.length == 11
        else -> false
    }

    private fun normalizeEan(content: String, length: Int): String {
        val digits = content.trim()
        require(digits.all { it.isDigit() }) {
            "Il numero deve contenere solo cifre (${length - 1} o $length per EAN-${if (length == 8) 8 else 13})."
        }
        return when (digits.length) {
            length - 1 -> digits + eanCheckDigit(digits, startWeight1 = true)
            length -> {
                val given = digits.last().digitToInt()
                val computed = eanCheckDigit(digits.dropLast(1), startWeight1 = true).digitToInt()
                require(given == computed) { "Cifra di controllo EAN non valida." }
                digits
            }
            else -> throw IllegalArgumentException(
                "Il numero deve avere ${length - 1} o $length cifre per EAN-${if (length == 8) 8 else 13}."
            )
        }
    }

    private fun normalizeUpc(content: String): String {
        val digits = content.trim()
        require(digits.all { it.isDigit() }) {
            "Il numero UPC deve contenere solo cifre (11 o 12)."
        }
        return when (digits.length) {
            11 -> digits + eanCheckDigit(digits, startWeight1 = false)
            12 -> {
                val given = digits.last().digitToInt()
                val computed = eanCheckDigit(digits.dropLast(1), startWeight1 = false).digitToInt()
                require(given == computed) { "Cifra di controllo UPC non valida." }
                digits
            }
            else -> throw IllegalArgumentException("Il numero UPC deve avere 11 o 12 cifre.")
        }
    }

    private fun eanCheckDigit(body: String, startWeight1: Boolean): Char {
        var sum = 0
        for (i in body.indices) {
            val d = body[i].digitToInt()
            val weight = if ((i % 2 == 0) == startWeight1) 1 else 3
            sum += d * weight
        }
        return ((10 - (sum % 10)) % 10 + '0'.code).toChar()
    }

    fun generate(
        rawContent: String,
        format: BarcodeFormatType,
        widthPx: Int,
        heightPx: Int,
        darkColor: Int = AndroidColor.BLACK,
        lightColor: Int = AndroidColor.WHITE
    ): Bitmap {
        val content = normalize(rawContent, format)
        val zxFormat = when (format) {
            BarcodeFormatType.EAN13 -> BarcodeFormat.EAN_13
            BarcodeFormatType.EAN8 -> BarcodeFormat.EAN_8
            BarcodeFormatType.UPC_A -> BarcodeFormat.UPC_A
            BarcodeFormatType.CODE_128 -> BarcodeFormat.CODE_128
            BarcodeFormatType.CODE_39 -> BarcodeFormat.CODE_39
            BarcodeFormatType.CODABAR -> BarcodeFormat.CODABAR
            BarcodeFormatType.QR_CODE -> BarcodeFormat.QR_CODE
        }

        val matrix: BitMatrix = try {
            MultiFormatWriter().encode(content, zxFormat, widthPx, heightPx)
        } catch (e: WriterException) {
            throw IllegalArgumentException("Contenuto non codificabile con questo formato.", e)
        }

        val pixels = IntArray(widthPx * heightPx)
        for (y in 0 until heightPx) {
            val offset = y * widthPx
            for (x in 0 until widthPx) {
                pixels[offset + x] = if (matrix[x, y]) darkColor else lightColor
            }
        }
        return Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, widthPx, 0, 0, widthPx, heightPx)
        }
    }

    /** Prova a leggere un codice a barre/QR da una bitmap (foto o galleria). */
    fun decode(bitmap: Bitmap): DecodeResult? {
        val normalized = downscaleIfNeeded(bitmap)
        listOf(0f, 90f, 180f, 270f).forEach { angle ->
            val candidate = if (angle == 0f) normalized else rotate(normalized, angle)
            decodeOnce(candidate)?.let { return it }
        }
        return null
    }

    private fun downscaleIfNeeded(src: Bitmap): Bitmap {
        val maxDim = 1600
        val largest = max(src.width, src.height)
        if (largest <= maxDim) return src
        val scale = maxDim.toFloat() / largest
        val w = (src.width * scale).toInt().coerceAtLeast(1)
        val h = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }

    private fun rotate(src: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    private fun decodeOnce(candidate: Bitmap): DecodeResult? {
        val width = candidate.width
        val height = candidate.height
        val pixels = IntArray(width * height)
        candidate.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binary = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        reader.setHints(
            hashMapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODABAR,
                    BarcodeFormat.QR_CODE
                )
            )
        )
        return try {
            reader.decodeWithState(binary)?.let { DecodeResult(it.text, it.barcodeFormat.toAppFormat()) }
        } catch (_: NotFoundException) {
            null
        } finally {
            reader.reset()
        }
    }

    private fun BarcodeFormat.toAppFormat(): BarcodeFormatType = when (this) {
        BarcodeFormat.EAN_13 -> BarcodeFormatType.EAN13
        BarcodeFormat.EAN_8 -> BarcodeFormatType.EAN8
        BarcodeFormat.UPC_A -> BarcodeFormatType.UPC_A
        BarcodeFormat.CODE_128 -> BarcodeFormatType.CODE_128
        BarcodeFormat.CODE_39 -> BarcodeFormatType.CODE_39
        BarcodeFormat.CODABAR -> BarcodeFormatType.CODABAR
        BarcodeFormat.QR_CODE -> BarcodeFormatType.QR_CODE
        else -> BarcodeFormatType.CODE_128
    }
}