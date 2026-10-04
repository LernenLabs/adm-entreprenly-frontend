package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.SegmentedControl
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ErrorBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton

/**
 * Add / edit product sheet content. In edit mode ([initial] != null) the
 * measure type and stock are read-only: stock is managed through lots, and
 * hidden fields are re-sent so the backend PUT never wipes them.
 */
@Composable
fun ProductFormSheet(
    viewModel: InventoryViewModel,
    initial: Product?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    currentStockText: String = ""
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
    var nameTouched by rememberSaveable(initial?.key) { mutableStateOf(false) }
    var priceTouched by rememberSaveable(initial?.key) { mutableStateOf(false) }
    var stockTouched by rememberSaveable(initial?.key) { mutableStateOf(false) }
    var submitted by rememberSaveable(initial?.key) { mutableStateOf(false) }

    val form by viewModel.productForm.collectAsState()
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

    fun showNameError() = (nameTouched || submitted) && nameError != null
    fun showPriceError() = (priceTouched || submitted) && priceError != null
    fun showStockError() = (stockTouched || submitted) && stockError != null && !editing

    val valid = nameError == null && priceError == null && (editing || stockError == null)

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
                            description = current.description,
                            codeQR = current.codeQR,
                            weightGrams = current.weightGrams,
                            brand = current.brand
                        )
                    )
                } else {
                    viewModel.createProduct(
                        CreateProductCommand(
                            name = name.trim(),
                            type = type,
                            price = priceValue!!,
                            initialStock = stockValue ?: 0.0
                        )
                    )
                }
            },
            enabled = valid && !form.loading
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
