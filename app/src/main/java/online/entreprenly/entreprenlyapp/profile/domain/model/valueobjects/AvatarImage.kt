package online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects

import java.io.ByteArrayOutputStream
import java.io.InputStream

class AvatarImage(val bytes: ByteArray, val mimeType: String) {
    class TooLarge : IllegalArgumentException()

    companion object {
        private const val maxBytes = 5 * 1024 * 1024
        val supportedTypes = setOf("image/jpeg", "image/png", "image/webp")

        fun readBytes(input: InputStream): ByteArray {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count == -1) return output.toByteArray()
                if (output.size() + count > maxBytes) throw TooLarge()
                output.write(buffer, 0, count)
            }
        }
    }
}
