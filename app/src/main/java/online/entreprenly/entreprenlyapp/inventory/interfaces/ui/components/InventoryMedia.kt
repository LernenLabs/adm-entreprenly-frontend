package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.ByteArrayOutputStream
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.entreprenly.entreprenlyapp.BuildConfig
import online.entreprenly.entreprenlyapp.R

/**
 * Turns a product imageUrl into an address Coil can load: external links stay as they are and
 * photos uploaded to the backend (paths such as /api/v1/inventory-product-images/…) are resolved
 * against the API base URL.
 */
fun resolveProductImageUrl(imageUrl: String?): String? {
    val url = imageUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (!url.startsWith("/")) return url
    return runCatching { URI(BuildConfig.API_BASE_URL).resolve(url).toString() }.getOrNull()
}

/** True for the path the backend returns after uploading a photo. */
fun isUploadedProductImage(imageUrl: String): Boolean =
    imageUrl.trim().startsWith("/api/v1/inventory-product-images/")

/** Size shared by every product thumbnail in inventory lists, so all rows line up. */
val ProductThumbnailSize: Dp = 52.dp

private val ThumbnailShape = RoundedCornerShape(12.dp)

/**
 * Product photo in a rounded square. Falls back to the initials avatar while the photo
 * loads, when it fails, or when the product has no photo.
 */
@Composable
fun ProductThumbnail(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = ProductThumbnailSize
) {
    val url = resolveProductImageUrl(imageUrl)
    var failed by remember(url) { mutableStateOf(false) }
    Box(
        modifier
            .size(size)
            .clip(ThumbnailShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // The initials stay underneath, so they show until the photo is drawn over them.
        ProductAvatar(name, size = size)
        if (url != null && !failed) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { failed = true },
                modifier = Modifier.size(size)
            )
        }
    }
}

/** Wide product photo for the detail screen and the form preview. Hidden when there is no URL. */
@Composable
fun ProductPhoto(imageUrl: String?, modifier: Modifier = Modifier) {
    val url = resolveProductImageUrl(imageUrl) ?: return
    var failed by remember(url) { mutableStateOf(false) }
    if (failed) return
    AsyncImage(
        model = url,
        contentDescription = stringResource(R.string.inventory_photo_description),
        contentScale = ContentScale.Fit,
        onError = { failed = true },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(ThumbnailShape)
            .background(MaterialTheme.colorScheme.surface)
    )
}

/**
 * Returns a function that opens the camera QR scanner. [onScanned] receives the trimmed
 * content of the code; a cancelled or empty scan is ignored. The scanner screen asks for the
 * camera permission itself.
 */
@Composable
fun rememberQrScanner(onScanned: (String) -> Unit): () -> Unit {
    val callback by rememberUpdatedState(onScanned)
    val prompt = stringResource(R.string.inventory_scan_prompt)
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.trim()?.takeIf { it.isNotEmpty() }?.let(callback)
    }
    return remember(launcher, prompt) {
        {
            launcher.launch(
                ScanOptions()
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    .setPrompt(prompt)
                    .setBeepEnabled(true)
                    .setOrientationLocked(false)
            )
        }
    }
}

/** Longest side, in pixels, of a gallery photo once it is prepared for upload. */
private const val MAX_PHOTO_SIDE = 1600

/** Upload limit of the backend is 5 MB; stay below it with some margin. */
private const val MAX_PHOTO_BYTES = 4_500_000

/**
 * Returns a function that opens the gallery photo picker. The chosen photo is scaled down to
 * [MAX_PHOTO_SIDE] and re-encoded as JPEG before [onPicked] receives its bytes and MIME type;
 * [onError] runs when the photo cannot be read. No permission is needed.
 */
@Composable
fun rememberGalleryPhotoPicker(
    onPicked: (bytes: ByteArray, mimeType: String) -> Unit,
    onError: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picked by rememberUpdatedState(onPicked)
    val failed by rememberUpdatedState(onError)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = withContext(Dispatchers.IO) { runCatching { preparePhoto(context, uri) }.getOrNull() }
            if (bytes == null) failed() else picked(bytes, "image/jpeg")
        }
    }
    return remember(launcher) {
        { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    }
}

/** Decodes [uri] at a reduced size and encodes it as a JPEG that fits the upload limit. */
private fun preparePhoto(context: Context, uri: Uri): ByteArray? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_PHOTO_SIDE) sample *= 2
    val decoded = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null
    val longest = maxOf(decoded.width, decoded.height)
    val bitmap = if (longest > MAX_PHOTO_SIDE) {
        val scale = MAX_PHOTO_SIDE.toFloat() / longest
        Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)
    } else {
        decoded
    }
    var quality = 88
    while (true) {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        if (out.size() <= MAX_PHOTO_BYTES || quality <= 40) {
            return out.toByteArray().takeIf { it.size <= MAX_PHOTO_BYTES }
        }
        quality -= 12
    }
}
