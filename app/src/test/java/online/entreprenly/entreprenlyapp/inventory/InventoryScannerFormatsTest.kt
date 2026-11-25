package online.entreprenly.entreprenlyapp.inventory

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryScanFormats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks that the decoder configured like the inventory scanner reads package barcodes, not
 * only QR codes, also when the barcode is small and away from the center of the frame.
 */
class InventoryScannerFormatsTest {

    private fun hints(tryHarder: Boolean): Map<DecodeHintType, Any> = buildMap {
        put(DecodeHintType.POSSIBLE_FORMATS, InventoryScanFormats.map(BarcodeFormat::valueOf))
        if (tryHarder) put(DecodeHintType.TRY_HARDER, true)
    }

    private fun render(text: String, format: BarcodeFormat, width: Int, height: Int): BitMatrix =
        MultiFormatWriter().encode(text, format, width, height, mapOf(EncodeHintType.MARGIN to 10))

    /** Draws [matrix] at ([left], [top]) of a white [frameWidth] x [frameHeight] camera-like frame. */
    private fun decode(
        matrix: BitMatrix,
        frameWidth: Int = matrix.width,
        frameHeight: Int = matrix.height,
        left: Int = 0,
        top: Int = 0,
        tryHarder: Boolean = true
    ): String {
        val pixels = IntArray(frameWidth * frameHeight) { 0xFFFFFFFF.toInt() }
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
            if (matrix[x, y]) pixels[(top + y) * frameWidth + (left + x)] = 0xFF000000.toInt()
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(frameWidth, frameHeight, pixels)))
        return MultiFormatReader().decode(bitmap, hints(tryHarder)).text
    }

    @Test
    fun scannerReadsPackageBarcodesAndQrCodes() {
        assertEquals("7750236173896", decode(render("7750236173896", BarcodeFormat.EAN_13, 400, 160)))
        assertEquals("40822938", decode(render("40822938", BarcodeFormat.EAN_8, 300, 140)))
        assertEquals("012345678905", decode(render("012345678905", BarcodeFormat.UPC_A, 400, 160)))
        assertEquals("QR-UP-0004", decode(render("QR-UP-0004", BarcodeFormat.QR_CODE, 300, 300)))
    }

    @Test
    fun smallOffCenterBarcodeNeedsTheTryHarderHint() {
        // A thin barcode near the top of the frame, as when the package is not centered.
        val barcode = render("7750236173896", BarcodeFormat.EAN_13, 300, 30)
        assertEquals(
            "7750236173896",
            decode(barcode, frameWidth = 400, frameHeight = 400, left = 50, top = 40, tryHarder = true)
        )
        val missedWithoutHint = runCatching {
            decode(barcode, frameWidth = 400, frameHeight = 400, left = 50, top = 40, tryHarder = false)
        }.exceptionOrNull() is NotFoundException
        assertTrue("Without TRY_HARDER only a few central rows are scanned", missedWithoutHint)
    }
}
