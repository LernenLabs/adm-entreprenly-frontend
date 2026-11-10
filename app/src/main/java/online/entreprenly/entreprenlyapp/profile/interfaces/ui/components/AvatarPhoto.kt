package online.entreprenly.entreprenlyapp.profile.interfaces.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.entreprenly.entreprenlyapp.BuildConfig
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AvatarImage
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.FormSubmit
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton

@Composable
fun ProfileAvatar(initials: String, url: String?) {
    val bitmap by produceState<Bitmap?>(null, url) {
        value = null
        if (url.isNullOrBlank()) return@produceState
        value = withContext(Dispatchers.IO) {
            runCatching {
                val address = URI(BuildConfig.API_BASE_URL).resolve(url)
                val https = address.scheme.equals("https", ignoreCase = true)
                val localAv2 = BuildConfig.BUILD_TYPE == "av2" && address.scheme == "http" &&
                    address.host in setOf("127.0.0.1", "10.0.2.2")
                require(address.host != null && (https || localAv2))
                val connection = address.toURL().openConnection() as HttpURLConnection
                try {
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
                    val bytes = connection.inputStream.use(AvatarImage::readBytes)
                    decodeAvatar(bytes).second
                } finally {
                    connection.disconnect()
                }
            }.getOrNull()
        }
    }
    val image = bitmap
    if (image == null) InitialsAvatar(initials, 76.dp) else AvatarBitmap(image)
}

@Composable
fun AvatarPhotoForm(
    form: FormState,
    enabled: Boolean,
    onSelectionChanged: () -> Unit,
    onUpload: (AvatarImage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cameraFile by rememberSaveable { mutableStateOf<String?>(null) }
    var photo by remember { mutableStateOf<Pair<AvatarImage, Bitmap>?>(null) }
    var error by remember { mutableStateOf<Int?>(null) }
    var reading by remember { mutableStateOf(false) }

    LaunchedEffect(form.success) {
        if (form.success != null) photo = null
    }

    fun readPhoto(uri: Uri, temporaryFile: File? = null) {
        onSelectionChanged()
        reading = true
        error = null
        photo = null
        scope.launch {
            try {
                photo = withContext(Dispatchers.IO) {
                    val bytes = requireNotNull(context.contentResolver.openInputStream(uri))
                        .use(AvatarImage::readBytes)
                    val (mimeType, preview) = decodeAvatar(bytes)
                    AvatarImage(bytes, mimeType) to preview
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: AvatarImage.TooLarge) {
                error = R.string.profile_avatar_size
            } catch (_: SecurityException) {
                error = R.string.profile_avatar_access
            } catch (_: Exception) {
                error = R.string.profile_avatar_format
            } finally {
                reading = false
                temporaryFile?.delete()
            }
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) readPhoto(uri)
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val file = cameraFile?.let(::File)
        cameraFile = null
        if (file != null) {
            if (captured) readPhoto(Uri.fromFile(file), file) else file.delete()
        }
    }

    Text(stringResource(R.string.profile_avatar_title), style = MaterialTheme.typography.titleMedium)
    photo?.let { AvatarBitmap(it.second) }
    error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
    if (reading) Text(stringResource(R.string.profile_avatar_reading))
    val canSelect = enabled && !reading && !form.loading
    SecondaryPillButton(
        stringResource(R.string.profile_avatar_gallery),
        onClick = {
            runCatching { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                .onFailure { error = R.string.profile_avatar_access }
        },
        enabled = canSelect
    )
    SecondaryPillButton(
        stringResource(R.string.profile_avatar_camera),
        onClick = {
            runCatching {
                val directory = File(context.cacheDir, "profile-camera").apply { mkdirs() }
                val file = File.createTempFile("profile-", ".jpg", directory)
                cameraFile = file.absolutePath
                val uri = FileProvider.getUriForFile(context, context.packageName + ".profile-files", file)
                takePhoto.launch(uri)
            }.onFailure {
                cameraFile?.let { File(it).delete() }
                cameraFile = null
                error = R.string.profile_avatar_access
            }
        },
        enabled = canSelect
    )
    val selection = photo
    if (selection != null && enabled) {
        FormSubmit(form, stringResource(R.string.profile_avatar_upload), { onUpload(selection.first) })
    } else {
        form.success?.let { Text(it.asString()) }
    }
}

@Composable
private fun AvatarBitmap(bitmap: Bitmap) {
    Image(
        bitmap.asImageBitmap(),
        contentDescription = stringResource(R.string.profile_avatar_title),
        modifier = Modifier.size(76.dp).clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

private fun decodeAvatar(bytes: ByteArray): Pair<String, Bitmap> {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    val mimeType = requireNotNull(options.outMimeType)
    require(mimeType in AvatarImage.supportedTypes && options.outWidth > 0 && options.outHeight > 0)
    val longestSide = maxOf(options.outWidth, options.outHeight)
    val sample = Integer.highestOneBit(maxOf(1, longestSide / 512))
    val bitmap = requireNotNull(BitmapFactory.decodeByteArray(
        bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample }
    ))
    return mimeType to bitmap
}
