package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryTextField
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.measureLabel
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ErrorBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton

private val SheetDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT)

/**
 * Create-lot sheet content (US-03). [preselected] with [locked] comes from an
 * out-of-stock product: the product is fixed and cannot be changed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotFormSheet(
    viewModel: InventoryViewModel,
    products: List<Product>,
    preselected: Product?,
    locked: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = InventoryRules.todayUtc()
    var query by rememberSaveable(preselected?.key) { mutableStateOf(preselected?.name.orEmpty()) }
    var selectedKey by rememberSaveable(preselected?.key) { mutableStateOf(preselected?.key) }
    var selectorTouched by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var codeQR by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var quantityTouched by rememberSaveable { mutableStateOf(false) }
    // Dates are kept as UTC-millis Longs: LocalDate is not Bundle-saveable.
    var entryMillis by rememberSaveable {
        mutableLongStateOf(InventoryRules.localDateToInstant(today).toEpochMilli())
    }
    var expiryMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var expiryTouched by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var picking by rememberSaveable { mutableStateOf<String?>(null) }

    val entryDate = Instant.ofEpochMilli(entryMillis).atZone(ZoneOffset.UTC).toLocalDate()
    val expiryDate = expiryMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }

    val form by viewModel.lotForm.collectAsState()
    LaunchedEffect(preselected?.key) { viewModel.clearLotForm() }
    LaunchedEffect(form.success) {
        if (form.success != null) {
            onClose()
            viewModel.clearLotForm()
        }
    }

    val selected = products.firstOrNull { it.key == selectedKey } ?: preselected?.takeIf { locked }
    val effective = selected
    val matches = if (query.isBlank()) products else products.filter { InventoryRules.matchesSearch(it, query) }

    val productError = (effective == null).takeIf { it }?.let {
        stringResource(R.string.inventory_lot_select_product)
    }
    val quantityError = (effective == null || !InventoryRules.isValidLotQuantity(effective.type, quantity))
        .takeIf { it }?.let { stringResource(R.string.inventory_form_required) }
    val expiryError = if (effective?.type == ProductType.UNIT) {
        (!InventoryRules.isValidExpiry(expiryDate, entryDate, today)).takeIf { it }?.let {
            stringResource(R.string.inventory_form_required)
        }
    } else null

    fun showProductError() = (selectorTouched || submitted) && productError != null
    fun showQuantityError() = (quantityTouched || submitted) && quantityError != null
    fun showExpiryError() = (expiryTouched || submitted) && expiryError != null

    val valid = productError == null && quantityError == null && expiryError == null

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.inventory_lot_product),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (locked && preselected != null) {
                InventoryTextField(
                    label = "",
                    value = preselected.name,
                    onChange = {},
                    enabled = false
                )
            } else {
                InventoryTextField(
                    label = "",
                    value = query,
                    onChange = {
                        query = it
                        selectedKey = null
                        expanded = true
                    },
                    placeholder = stringResource(R.string.inventory_lot_search_product),
                    enabled = !form.loading,
                    isError = showProductError(),
                    errorText = if (showProductError()) productError else null,
                    modifier = Modifier.onFocusChanged {
                        if (!it.isFocused) {
                            selectorTouched = true
                            if (selectedKey == null && matches.size == 1) {
                                selectedKey = matches.first().key
                                query = matches.first().name
                            }
                            expanded = false
                        } else {
                            expanded = true
                        }
                    }
                )
                if (expanded && !form.loading) {
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 180.dp)) {
                        items(matches.take(8), key = { it.key }) { product ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedKey = product.key
                                        query = product.name
                                        expanded = false
                                        selectorTouched = true
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(product.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    measureLabel(product.type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InventoryTextField(
                label = stringResource(R.string.inventory_lot_number),
                value = codeQR,
                onChange = { codeQR = it },
                enabled = !form.loading,
                modifier = Modifier.weight(1f)
            )
            InventoryTextField(
                label = stringResource(R.string.inventory_lot_quantity),
                value = quantity,
                onChange = { quantity = it },
                keyboardType = if ((effective?.type ?: ProductType.UNIT) == ProductType.UNIT) {
                    KeyboardType.Number
                } else {
                    KeyboardType.Decimal
                },
                enabled = !form.loading,
                isError = showQuantityError(),
                errorText = if (showQuantityError()) quantityError else null,
                suffix = if ((effective?.type ?: ProductType.UNIT) == ProductType.UNIT) "und" else "kg",
                modifier = Modifier.weight(1f).onFocusChanged { if (!it.isFocused) quantityTouched = true }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DateCell(
                label = stringResource(R.string.inventory_lot_entry_date),
                date = entryDate,
                enabled = !form.loading,
                onClick = { picking = "entry" },
                modifier = Modifier.weight(1f)
            )
            if ((effective?.type ?: ProductType.UNIT) == ProductType.UNIT) {
                DateCell(
                    label = stringResource(R.string.inventory_lot_expiry),
                    date = expiryDate,
                    enabled = !form.loading,
                    isError = showExpiryError(),
                    errorText = if (showExpiryError()) expiryError else null,
                    onClick = {
                        expiryTouched = true
                        picking = "expiry"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        form.error?.let { ErrorBanner(title = it.asString(), message = null) }
        PrimaryPillButton(
            text = stringResource(R.string.inventory_lot_add),
            onClick = {
                submitted = true
                selectorTouched = true
                quantityTouched = true
                expiryTouched = true
                val product = effective
                if (!valid || form.loading || product == null) return@PrimaryPillButton
                val qtyUnits = if (product.type == ProductType.UNIT) {
                    InventoryRules.parseUnitStock(quantity) ?: return@PrimaryPillButton
                } else 0
                val qtyKg = if (product.type == ProductType.WEIGHT) {
                    InventoryRules.parseWeightStock(quantity) ?: return@PrimaryPillButton
                } else 0.0
                viewModel.createLot(
                    CreateLotCommand(
                        productType = product.type,
                        productId = product.id,
                        codeQR = codeQR.takeIf { it.isNotBlank() },
                        entryDate = InventoryRules.localDateToInstant(entryDate),
                        expiryDate = if (product.type == ProductType.UNIT) {
                            expiryDate?.let(InventoryRules::localDateToInstant)
                        } else null,
                        quantityUnits = qtyUnits,
                        quantityKg = qtyKg
                    )
                )
            },
            enabled = valid && !form.loading
        )
    }

    if (picking != null) {
        val forExpiry = picking == "expiry"
        val todayStart = InventoryRules.localDateToInstant(today).toEpochMilli()
        val entryStart = entryMillis
        val initialMillis = if (forExpiry) expiryMillis ?: entryStart else entryStart
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    if (utcTimeMillis < todayStart) return false
                    if (forExpiry && utcTimeMillis < entryStart) return false
                    return true
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            if (forExpiry) expiryMillis = millis else entryMillis = millis
                        }
                        picking = null
                    }
                ) { Text(stringResource(R.string.inventory_lot_confirm_date)) }
            }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.inventory_lot_date_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.inventory_lot_date_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DatePicker(state = dateState)
            }
        }
    }
}

@Composable
private fun DateCell(
    label: String,
    date: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                date?.format(SheetDateFormat) ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        if (errorText != null) {
            Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
