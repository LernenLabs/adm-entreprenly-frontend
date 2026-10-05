package online.entreprenly.entreprenlyapp.chatbot.interfaces.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.BannerKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ErrorBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.MessageBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccessContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarning
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarningContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

// WhatsApp keeps the first pairing QR for about a minute and later ones for about 20 seconds.
private const val FIRST_QR_SECONDS = 60
private const val NEXT_QR_SECONDS = 20
private const val RENEWED_BANNER_SECONDS = 5

/**
 * "Link WhatsApp Business" card: the QR the bridge generated plus the steps to scan it.
 * [qr] is a `data:image` URL or the raw QR payload; null while the bridge has not sent one yet.
 */
@Composable
fun LinkWhatsAppContent(qr: String?, modifier: Modifier = Modifier) {
    var previousQr by remember { mutableStateOf<String?>(null) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var renewed by remember { mutableStateOf(false) }

    // Each new QR restarts the countdown; a replacement means the previous one expired.
    LaunchedEffect(qr) {
        if (qr == null) return@LaunchedEffect
        val ttl = if (previousQr == null) FIRST_QR_SECONDS else NEXT_QR_SECONDS
        renewed = previousQr != null
        previousQr = qr
        secondsLeft = ttl
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
            if (ttl - secondsLeft >= RENEWED_BANNER_SECONDS) renewed = false
        }
    }
    val expired = qr != null && secondsLeft == 0

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (renewed || expired) {
            ErrorBanner(
                title = stringResource(R.string.chatbot_link_code_expired),
                message = stringResource(
                    if (renewed) R.string.chatbot_link_code_renewed else R.string.chatbot_link_code_waiting
                )
            )
        }
        AppCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier.size(52.dp).background(StatusSuccessContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    stringResource(R.string.chatbot_link_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.chatbot_link_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.muted,
                    textAlign = TextAlign.Center
                )
                if (qr != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Timer, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.chatbot_link_expires_in, secondsLeft / 60, secondsLeft % 60),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusWarning,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                QrImage(qr)
            }
        }
        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    R.string.chatbot_link_step_open,
                    R.string.chatbot_link_step_settings,
                    R.string.chatbot_link_step_link,
                    R.string.chatbot_link_step_scan
                ).forEachIndexed { index, step ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(22.dp).background(StatusWarningContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                        }
                        Text(stringResource(step), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun QrImage(qr: String?) {
    val bitmap = remember(qr) { qr?.let(::qrBitmap) }
    Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.chatbot_link_qr_description),
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator()
                Text(
                    stringResource(R.string.chatbot_link_waiting_qr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.extraColors.muted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** The bridge may send a ready image (data URL) or the raw payload, which is drawn here. */
private fun qrBitmap(qr: String): ImageBitmap? {
    if (qr.startsWith("data:")) return decodeDataUrl(qr)
    return runCatching {
        val matrix = QRCodeWriter().encode(qr, BarcodeFormat.QR_CODE, 0, 0, mapOf(EncodeHintType.MARGIN to 1))
        val pixels = IntArray(matrix.width * matrix.height) { i ->
            if (matrix[i % matrix.width, i / matrix.width]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
        Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888).asImageBitmap()
    }.getOrNull()
}

/** Shown once after the link succeeds; disappears on its own. */
@Composable
fun LinkedBanner(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        delay(6_000)
        onDismiss()
    }
    MessageBanner(
        title = stringResource(R.string.chatbot_linked_title),
        message = stringResource(R.string.chatbot_linked_message),
        kind = BannerKind.SUCCESS,
        modifier = modifier
    )
}

/** WhatsApp was linked before (there is history) but the session is no longer active. */
@Composable
fun SessionDisconnectedBanner(onRelink: () -> Unit, modifier: Modifier = Modifier) {
    val error = MaterialTheme.colorScheme.error
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.extraColors.errorBackground,
        border = BorderStroke(1.dp, error.copy(alpha = 0.4f))
    ) {
        Row(Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LinkOff, contentDescription = null, tint = error, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    stringResource(R.string.chatbot_session_expired_title),
                    color = error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.chatbot_session_expired_message),
                    color = MaterialTheme.extraColors.muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onRelink) {
                Text(stringResource(R.string.chatbot_relink), color = error, fontWeight = FontWeight.Bold)
            }
        }
    }
}
