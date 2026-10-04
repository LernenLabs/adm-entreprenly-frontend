package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import online.entreprenly.entreprenlyapp.R

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dayTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
private val fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
fun formatMoney(amount: Double): String = stringResource(R.string.chatbot_money, amount)

/** "10:18" for today, "02/10 10:18" otherwise. */
fun Instant.shortLabel(): String {
    val local = atZone(ZoneId.systemDefault())
    return if (local.toLocalDate() == LocalDate.now()) timeFormatter.format(local) else dayTimeFormatter.format(local)
}

fun Instant.fullLabel(): String = fullFormatter.format(atZone(ZoneId.systemDefault()))

/** "Andrea Torres" -> "AT"; phone numbers or blanks fall back to "?". */
fun initialsOf(name: String?): String =
    name.orEmpty().split(' ').filter { it.firstOrNull()?.isLetter() == true }
        .take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** Backend sends "YAPE", "PLIN"... -> "Yape", "Plin". */
fun paymentLabel(method: String?): String? =
    method?.takeIf { it.isNotBlank() }?.lowercase()?.replaceFirstChar { it.uppercase() }

/** Decodes a `data:image/...;base64,` URL (or raw base64) into a bitmap; null when invalid. */
fun decodeDataUrl(dataUrl: String?): ImageBitmap? {
    if (dataUrl.isNullOrBlank()) return null
    return runCatching {
        val bytes = Base64.decode(dataUrl.substringAfter("base64,"), Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}
