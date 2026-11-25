package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.scanner

import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.Decoder
import com.journeyapps.barcodescanner.DecoderFactory

/**
 * Wraps the scanner's decoders so every camera frame is also tried turned 90 degrees. Without
 * it, barcodes are only read when their bars run up and down on the screen.
 */
class RotatingDecoderFactory(private val delegate: DecoderFactory) : DecoderFactory {
    override fun createDecoder(baseHints: Map<DecodeHintType, *>): Decoder =
        RotatingDecoder(delegate.createDecoder(baseHints))
}

/** Tries [inner] on the frame as it is and, when nothing is found, on the frame turned 90 degrees. */
class RotatingDecoder(private val inner: Decoder) : Decoder(MultiFormatReader()) {

    override fun decode(source: LuminanceSource): Result? =
        inner.decode(source) ?: inner.decode(RotatedLuminanceSource(source))

    override fun getPossibleResultPoints(): List<ResultPoint> = inner.possibleResultPoints

    override fun foundPossibleResultPoint(point: ResultPoint) = inner.foundPossibleResultPoint(point)
}
