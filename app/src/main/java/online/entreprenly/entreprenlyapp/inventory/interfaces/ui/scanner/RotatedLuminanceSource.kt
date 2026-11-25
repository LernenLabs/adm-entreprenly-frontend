package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.scanner

import com.google.zxing.LuminanceSource

/**
 * [source] turned 90 degrees clockwise. Barcode readers only scan rows, so a package barcode
 * whose bars run across the camera frame (a bottle held upright) is only found in this view.
 */
class RotatedLuminanceSource(source: LuminanceSource) : LuminanceSource(source.height, source.width) {

    private val pixels: ByteArray = rotate(source.matrix, source.width, source.height)

    override fun getRow(y: Int, row: ByteArray?): ByteArray {
        require(y in 0 until height) { "Requested row is outside the image: $y" }
        val out = if (row == null || row.size < width) ByteArray(width) else row
        System.arraycopy(pixels, y * width, out, 0, width)
        return out
    }

    override fun getMatrix(): ByteArray = pixels

    private companion object {
        /** Rotates a row-major [w] x [h] luminance matrix 90 degrees clockwise (result is [h] x [w]). */
        fun rotate(matrix: ByteArray, w: Int, h: Int): ByteArray {
            val out = ByteArray(w * h)
            for (y in 0 until h) {
                val rowStart = y * w
                val column = h - 1 - y
                for (x in 0 until w) out[x * h + column] = matrix[rowStart + x]
            }
            return out
        }
    }
}
