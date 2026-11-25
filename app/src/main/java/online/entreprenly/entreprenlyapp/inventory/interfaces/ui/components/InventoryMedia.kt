package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import online.entreprenly.entreprenlyapp.R

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
    val url = imageUrl?.trim()?.takeIf { it.isNotEmpty() }
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
    val url = imageUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return
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
