package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryTextField
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.ProductPhoto
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.isUploadedProductImage
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.rememberGalleryPhotoPicker
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.rememberQrScanner
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.SegmentedControl
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ErrorBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton

/**
 * Add / edit product sheet content. In edit mode ([initial] != null) the
 * measure type and stock are read-only: stock is managed through lots, and
 * fields the form does not show (weightGrams) are re-sent so the backend PUT
 * never wipes them. [initialCodeQR] pre-fills the QR code of a new product,
 * e.g. after scanning a code that is not registered yet.
 */
@Composable
fun ProductFormSheet(
    viewModel: InventoryViewModel,
    initial: Product?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    currentStockText: String = "",
    initialCodeQR: String? = null
) {
    val editing = initial != null
    var name by rememberSaveable(initial?.key) { mutableStateOf(initial?.name.orEmpty()) }
    var typeIndex by rememberSaveable(initial?.key) {
        mutableIntStateOf(if ((initial?.type ?: ProductType.UNIT) == ProductType.UNIT) 0 else 1)
    }
    var price by rememberSaveable(initial?.key) {
        mutableStateOf(if (initial == null) "" else trimPrice(initial.price))
    }
    var stock by rememberSaveable(initial?.key) { mutableStateOf("") }
    var description by rememberSaveable(initial?.key) { mutableStateOf(initial?.description.orEmpty()) }
    var brand by rememberSaveable(initial?.key) { mutableStateOf(initial?.brand.orEmpty()) }
    var codeQR by rememberSaveable(initial?.key) {
        mutableStateOf(initial?.codeQR ?: initialCodeQR.orEmpty())
    }
    var imageUrl by rememberSaveable(initial?.key) { mutableStateOf(initial?.imageUrl.orEmpty()) }
    val scanCode = rememberQrScanner { scanned ->
        codeQR = scanned
        if (!editing) viewModel.lookupBarcode(scanned)
    }
    // Data suggested for a scanned package barcode only fills the fields the seller left blank.
    val lookup by viewModel.barcodeLookup.collectAsState()
    val lookupApplies = !editing && lookup.barcode != null && lookup.barcode == codeQR.trim()
    LaunchedEffect(lookup.product) {
        val found = lookup.product ?: return@LaunchedEffect
        if (editing || found.barcode != codeQR.trim()) return@LaunchedEffect
        if (name.isBlank()) found.name?.let { name = it }
        if (brand.isBlank()) found.brand?.let { brand = it }
        if (description.isBlank()) found.description?.let { description = it }
        if (imageUrl.isBlank()) found.imageUrl?.let { imageUrl = it }
        typeIndex = 0
    }
    val form by viewModel.productForm.collectAsState()
    val photoUpload by viewModel.photoUpload.collectAsState()
    LaunchedEffect(initial?.key) { viewModel.clearPhotoUpload() }
    LaunchedEffect(photoUpload.imageUrl) {
        photoUpload.imageUrl?.let {
            imageUrl = it
            viewModel.clearPhotoUpload()
        }
    }
    val pickPhoto = rememberGalleryPhotoPicker(
        onPicked = viewModel::uploadProductPhoto,
        onError = { viewModel.reportPhotoError(UiText.Res(R.string.inventory_photo_read_failed)) }
    )
    val busy = form.loading || photoUpload.loading
    var nameTouched by rememberSaveable(initial?.key) { mutableStateOf(false) }
    var priceTouched by rememberSaveable(initial?.key) { mutableStateOf(false) }
    var stockTouched by rememberSaveable(initial?.key) { mutableStateOf(false) }
    var submitted by rememberSaveable(initial?.key) { mutableStateOf(false) }

    LaunchedEffect(initial?.key) { viewModel.clearProductForm() }
    LaunchedEffect(form.success) {
        if (form.success != null) {
            onClose()
            viewModel.clearProductForm()
        }
    }

    val type = if (typeIndex == 0) ProductType.UNIT else ProductType.WEIGHT
    val nameError = (!InventoryRules.isValidProductName(name)).takeIf { it }?.let {
        stringResource(R.string.inventory_form_required)
    }
    val priceValue = InventoryRules.parsePrice(price)
    val priceError = (priceValue == null).takeIf { it }?.let {
        stringResource(R.string.inventory_form_required)
    }
    val stockValue: Double? = if (type == ProductType.UNIT) {
        InventoryRules.parseUnitStock(stock)?.toDouble()
    } else {
        InventoryRules.parseWeightStock(stock)
    }
    val stockError = (stockValue == null).takeIf { it }?.let {
        stringResource(R.string.inventory_form_required)
    }

    val imageUrlError = imageUrl.trim().let {
        it.isNotEmpty() && !it.startsWith("https://") && !it.startsWith("http://") && !isUploadedProductImage(it)
    }.takeIf { it }?.let { stringResource(R.string.inventory_form_image_url_invalid) }

    fun showNameError() = (nameTouched || submitted) && nameError != null
    fun showPriceError() = (priceTouched || submitted) && priceError != null
    fun showStockError() = (stockTouched || submitted) && stockError != null && !editing

    val valid = nameError == null && priceError == null && imageUrlError == null &&
        (editing || stockError == null)

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InventoryTextField(
            label = stringResource(R.string.inventory_form_name),
            value = name,
            onChange = { name = it },
            placeholder = stringResource(R.string.inventory_form_name_hint),
            enabled = !form.loading,
            isError = showNameError(),
            errorText = if (showNameError()) nameError else null,
            modifier = Modifier.onFocusChanged { if (!it.isFocused) nameTouched = true }
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.inventory_form_measure),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            SegmentedControl(
                options = listOf(
                    stringResource(R.string.inventory_form_measure_unit),
                    stringResource(R.string.inventory_form_measure_weight)
                ),
                selected = typeIndex,
                onSelect = { if (!editing && !form.loading) typeIndex = it }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InventoryTextField(
                label = stringResource(R.string.inventory_form_price),
                value = price,
                onChange = { price = it },
                placeholder = stringResource(R.string.inventory_form_price_hint),
                keyboardType = KeyboardType.Decimal,
                enabled = !form.loading,
                isError = showPriceError(),
                errorText = if (showPriceError()) priceError else null,
                modifier = Modifier.weight(1f).onFocusChanged { if (!it.isFocused) priceTouched = true }
            )
            if (editing) {
                InventoryTextField(
                    label = stringResource(R.string.inventory_form_initial_stock),
                    value = currentStockText,
                    onChange = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
            } else {
                InventoryTextField(
                    label = stringResource(R.string.inventory_form_initial_stock),
                    value = stock,
                    onChange = { stock = it },
                    placeholder = stringResource(R.string.inventory_form_initial_stock_hint),
                    keyboardType = if (type == ProductType.UNIT) KeyboardType.Number else KeyboardType.Decimal,
                    enabled = !form.loading,
                    isError = showStockError(),
                    errorText = if (showStockError()) stockError else null,
                    suffix = if (type == ProductType.UNIT) "und" else "kg",
                    modifier = Modifier.weight(1f).onFocusChanged { if (!it.isFocused) stockTouched = true }
                )
            }
        }
        if (editing) {
            Text(
                stringResource(R.string.inventory_form_stock_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (type == ProductType.UNIT) {
            InventoryTextField(
                label = stringResource(R.string.inventory_form_brand),
                value = brand,
                onChange = { brand = it },
                placeholder = stringResource(R.string.inventory_form_brand_hint),
                enabled = !form.loading
            )
        }
        InventoryTextField(
            label = stringResource(R.string.inventory_form_description),
            value = description,
            onChange = { description = it },
            placeholder = stringResource(R.string.inventory_form_description_hint),
            enabled = !form.loading,
            singleLine = false
        )
        InventoryTextField(
            label = stringResource(R.string.inventory_form_code_qr),
            value = codeQR,
            onChange = { codeQR = it },
            placeholder = stringResource(R.string.inventory_form_code_qr_hint),
            enabled = !form.loading,
            trailing = {
                IconButton(onClick = scanCode, enabled = !form.loading) {
                    Icon(
                        Icons.Outlined.QrCodeScanner,
                        contentDescription = stringResource(R.string.inventory_scan_qr),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
        if (lookupApplies) {
            val (message, isError) = when {
                lookup.loading -> stringResource(R.string.inventory_barcode_searching) to false
                lookup.product != null -> stringResource(R.string.inventory_barcode_found) to false
                lookup.notFound -> stringResource(R.string.inventory_barcode_not_found) to false
                else -> stringResource(R.string.inventory_barcode_failed) to true
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (lookup.loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // A photo can be a pasted link or one uploaded from the gallery; both end up in imageUrl.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            InventoryTextField(
                label = stringResource(R.string.inventory_form_image_url),
                value = imageUrl,
                onChange = { imageUrl = it },
                placeholder = stringResource(R.string.inventory_form_image_url_hint),
                keyboardType = KeyboardType.Uri,
                enabled = !busy,
                isError = imageUrlError != null,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = pickPhoto,
                enabled = !busy,
                modifier = Modifier.height(56.dp)
            ) {
                if (photoUpload.loading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text(
                    stringResource(R.string.inventory_form_upload_photo),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        imageUrlError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        photoUpload.error?.let {
            Text(
                stringResource(R.string.inventory_photo_upload_failed, it.asString()),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (imageUrlError == null) ProductPhoto(imageUrl)
        form.error?.let { ErrorBanner(title = it.asString(), message = null) }
        PrimaryPillButton(
            text = stringResource(
                if (editing) R.string.inventory_form_save_changes else R.string.inventory_form_save
            ),
            onClick = {
                submitted = true
                nameTouched = true
                priceTouched = true
                stockTouched = true
                if (!valid || form.loading) return@PrimaryPillButton
                if (editing) {
                    val current = initial!!
                    viewModel.updateProduct(
                        UpdateProductCommand(
                            type = current.type,
                            id = current.id,
                            name = name.trim(),
                            price = priceValue!!,
                            description = description,
                            codeQR = codeQR,
                            weightGrams = current.weightGrams,
                            brand = if (current.type == ProductType.UNIT) brand else null,
                            imageUrl = imageUrl
                        )
                    )
                } else {
                    viewModel.createProduct(
                        CreateProductCommand(
                            name = name.trim(),
                            type = type,
                            price = priceValue!!,
                            initialStock = stockValue ?: 0.0,
                            description = description,
                            codeQR = codeQR,
                            brand = if (type == ProductType.UNIT) brand else null,
                            imageUrl = imageUrl
                        )
                    )
                }
            },
            enabled = valid && !busy
        )
        if (form.loading) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun trimPrice(price: Double): String =
    if (price == price.toLong().toDouble()) price.toLong().toString() else price.toString()
