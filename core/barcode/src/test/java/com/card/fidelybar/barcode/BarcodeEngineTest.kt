package com.card.fidelybar.barcode

import com.card.fidelybar.data.BarcodeFormatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class BarcodeEngineTest {

    @Test
    fun `detect EAN13 from 13 digits with valid check`() {
        assertEquals(BarcodeFormatType.EAN13, BarcodeEngine.detectFormat("5901234123457"))
    }

    @Test
    fun `detect EAN13 without check digit`() {
        assertEquals(BarcodeFormatType.EAN13, BarcodeEngine.detectFormat("590123412345"))
    }

    @Test
    fun `detect EAN8 from 8 digits with valid check`() {
        assertEquals(BarcodeFormatType.EAN8, BarcodeEngine.detectFormat("96385074"))
    }

    @Test
    fun `detect EAN8 without check digit`() {
        assertEquals(BarcodeFormatType.EAN8, BarcodeEngine.detectFormat("9638507"))
    }

    @Test
    fun `detect EAN13 body from 12 digits without check`() {
        assertEquals(BarcodeFormatType.EAN13, BarcodeEngine.detectFormat("590123412345"))
    }

    @Test
    fun `detect UPC from 12 digits with valid check`() {
        assertEquals(BarcodeFormatType.UPC_A, BarcodeEngine.detectFormat("036000291452"))
    }

    @Test
    fun `detect UPC without check digit`() {
        assertEquals(BarcodeFormatType.UPC_A, BarcodeEngine.detectFormat("03600029145"))
    }

    @Test
    fun `detect ITF from short even digit count`() {
        assertEquals(BarcodeFormatType.ITF, BarcodeEngine.detectFormat("04210000526442"))
    }

    @Test
    fun `detect Code128 fallback for odd digit count`() {
        assertEquals(BarcodeFormatType.CODE_128, BarcodeEngine.detectFormat("123456789"))
    }

    @Test
    fun `detect Code39 for alphanumeric short content`() {
        assertEquals(BarcodeFormatType.CODE_39, BarcodeEngine.detectFormat("ABC-123"))
    }

    @Test
    fun `detect Code128 for mixed alphanumeric`() {
        assertEquals(BarcodeFormatType.CODE_128, BarcodeEngine.detectFormat("X-92A!F"))
    }

    @Test
    fun `detect null for empty content`() {
        assertNull(BarcodeEngine.detectFormat(""))
        assertNull(BarcodeEngine.detectFormat("   "))
    }

    @Test
    fun `normalizeAuto resolves and keeps ITF`() {
        assertEquals("042100005264", BarcodeEngine.normalizeAuto("042100005264"))
    }

    @Test
    fun `normalizeAuto trims and keeps Code128 content`() {
        assertEquals("hello", BarcodeEngine.normalizeAuto("  hello  "))
    }

    @Test
    fun `normalize ITF requires digits`() {
        assertThrows(IllegalArgumentException::class.java) {
            BarcodeEngine.normalize("AB12", BarcodeFormatType.ITF)
        }
    }

    @Test
    fun `normalize ITF requires even count`() {
        assertThrows(IllegalArgumentException::class.java) {
            BarcodeEngine.normalize("123", BarcodeFormatType.ITF)
        }
    }

    @Test
    fun `normalize EAN13 trims and validates`() {
        assertEquals("5901234123457", BarcodeEngine.normalize(" 5901234123457 ", BarcodeFormatType.EAN13))
    }

    @Test
    fun `expected digits null for non-fixed numeric`() {
        assertNull(BarcodeEngine.expectedDigits(BarcodeFormatType.CODE_128))
        assertNull(BarcodeEngine.expectedDigits(BarcodeFormatType.ITF))
        assertNull(BarcodeEngine.expectedDigits(BarcodeFormatType.DATA_MATRIX))
    }

    @Test
    fun `expected digits map for fixed numeric`() {
        assertEquals(13, BarcodeEngine.expectedDigits(BarcodeFormatType.EAN13))
        assertEquals(8, BarcodeEngine.expectedDigits(BarcodeFormatType.EAN8))
        assertEquals(12, BarcodeEngine.expectedDigits(BarcodeFormatType.UPC_A))
    }

    @Test
    fun `isMissingCheckDigit for likely-body lengths`() {
        assertTrue(BarcodeEngine.isMissingCheckDigit("590123412345", BarcodeFormatType.EAN13))
        assertTrue(BarcodeEngine.isMissingCheckDigit("9638507", BarcodeFormatType.EAN8))
        assertTrue(BarcodeEngine.isMissingCheckDigit("03600029145", BarcodeFormatType.UPC_A))
    }
}