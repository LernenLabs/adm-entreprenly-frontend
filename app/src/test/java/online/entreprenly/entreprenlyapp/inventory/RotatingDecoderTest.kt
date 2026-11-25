package online.entreprenly.entreprenlyapp.inventory

import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.PlanarYUVLuminanceSource
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryScanFormats
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.scanner.RotatedLuminanceSource
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.scanner.RotatingDecoderFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The scanner must read a package barcode whether it is held upright or sideways. */
class RotatingDecoderTest {

    private val factory = DefaultDecoderFactory(
        InventoryScanFormats.map(BarcodeFormat::valueOf),
        mapOf(DecodeHintType.TRY_HARDER to true),
        null,
        0
    )

    /** A grayscale camera-like frame (as the scanner receives it) with a barcode drawn on it. */
    private fun frame(code: String, sideways: Boolean): PlanarYUVLuminanceSource {
        val bars = MultiFormatWriter().encode(code, BarcodeFormat.EAN_13, 360, 120, mapOf(EncodeHintType.MARGIN to 10))
        val w = 480
        val h = 640
        val yuv = ByteArray(w * h) { 0xFF.toByte() }
        for (y in 0 until bars.height) for (x in 0 until bars.width) {
            if (!bars[x, y]) continue
            // Sideways: the bars run across the frame, as with an upright bottle on a phone in portrait.
            val (px, py) = if (sideways) (60 + y) to (140 + x) else (60 + x) to (260 + y)
            yuv[py * w + px] = 0
        }
        return PlanarYUVLuminanceSource(yuv, w, h, 0, 0, w, h, false)
    }

    @Test
    fun readsUprightAndSidewaysBarcodes() {
        val decoder = RotatingDecoderFactory(factory).createDecoder(emptyMap<DecodeHintType, Any>())
        assertEquals("7401005914706", decoder.decode(frame("7401005914706", sideways = false))?.text)
        assertEquals("7753464000408", decoder.decode(frame("7753464000408", sideways = true))?.text)
    }

    @Test
    fun libraryDecoderAloneMissesSidewaysBarcodes() {
        val plain = factory.createDecoder(emptyMap<DecodeHintType, Any>())
        assertNull(plain.decode(frame("7753464000408", sideways = true)))
    }

    @Test
    fun rotatingTwiceFourTimesGivesTheSameImage() {
        val source = frame("7401005914706", sideways = false)
        var rotated: com.google.zxing.LuminanceSource = source
        repeat(4) { rotated = RotatedLuminanceSource(rotated) }
        assertEquals(source.width, rotated.width)
        assertArrayEquals(source.matrix, rotated.matrix)
    }
}
