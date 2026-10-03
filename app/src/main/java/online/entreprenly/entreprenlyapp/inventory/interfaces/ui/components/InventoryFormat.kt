package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.LotStatus
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType

/** "S/ 24.90" or "S/ 28.00/kg". */
@Composable
fun formatPrice(product: Product): String =
    if (product.type == ProductType.UNIT) stringResource(R.string.inventory_money, product.price)
    else stringResource(R.string.inventory_money_per_kg, product.price)

/** "18 und" or "3.2 kg". */
@Composable
fun formatStock(product: Product, stock: Double): String {
    if (product.type == ProductType.UNIT) {
        val qty = stock.toInt()
        return pluralStringResource(R.plurals.inventory_quantity_units, qty, qty)
    }
    return stringResource(R.string.inventory_quantity_kg, InventoryRules.formatKg(stock))
}

/** "Unidad" or "Peso (kg)". */
@Composable
fun measureLabel(type: ProductType): String =
    stringResource(
        if (type == ProductType.UNIT) R.string.inventory_measure_unit
        else R.string.inventory_measure_weight
    )

/** Card subtitle: "Unidad · S/ 24.90". */
@Composable
fun productSubtitle(product: Product): String =
    "${measureLabel(product.type)} · ${formatPrice(product)}"

/** "Quedan 4 unidades." / "Quedan 3.2 kg." */
@Composable
fun remainingText(product: Product, stock: Double): String =
    if (product.type == ProductType.UNIT) {
        stringResource(R.string.inventory_detail_remaining_units, stock.toInt().toString())
    } else {
        stringResource(R.string.inventory_detail_remaining_kg, InventoryRules.formatKg(stock))
    }

/** Lot row detail: "L-0245 · Vence 04/10/2026 · 12 und" (weight: "L-0230 · 40 kg"). */
@Composable
fun lotRowDetail(lot: Lot): String {
    val amount = unitAmount(lot)
        ?: stringResource(R.string.inventory_quantity_kg, InventoryRules.formatKg(lot.quantityKg))
    val expiry = lot.expiryDate
    return if (lot.type == ProductType.UNIT && expiry != null) {
        val date = InventoryRules.formatLotDate(expiry)
        "${lot.label} · ${stringResource(R.string.inventory_lot_row_expires, date)} · $amount"
    } else {
        "${lot.label} · $amount"
    }
}

/** Lot row inside the product detail: weight lots show the entry date. */
@Composable
fun productLotRowDetail(lot: Lot): String {
    val amount = unitAmount(lot)
        ?: stringResource(R.string.inventory_quantity_kg, InventoryRules.formatKg(lot.quantityKg))
    val expiry = lot.expiryDate
    return if (lot.type == ProductType.UNIT && expiry != null) {
        val date = InventoryRules.formatLotDate(expiry)
        "${stringResource(R.string.inventory_lot_row_expires, date)} · $amount"
    } else {
        val date = InventoryRules.formatLotDate(lot.entryDate)
        "${stringResource(R.string.inventory_lot_row_entered, date)} · $amount"
    }
}

@Composable
private fun unitAmount(lot: Lot): String? {
    if (lot.type != ProductType.UNIT) return null
    return pluralStringResource(R.plurals.inventory_quantity_units, lot.quantityUnits, lot.quantityUnits)
}

data class LotPillData(val text: String, val kind: LotPillKind)

/** Pill content for a lot: "Vence en N días" / "Vence hoy" / "Vencido" / "Activo" / "Agotado". */
@Composable
fun lotPill(lot: Lot, today: java.time.LocalDate = InventoryRules.todayUtc()): LotPillData {
    return when (InventoryRules.lotStatus(lot, today)) {
        LotStatus.OUT_OF_STOCK ->
            LotPillData(stringResource(R.string.inventory_pill_out_of_stock), LotPillKind.OUT)
        LotStatus.EXPIRED ->
            LotPillData(stringResource(R.string.inventory_pill_expired), LotPillKind.EXPIRED)
        LotStatus.EXPIRING_SOON -> {
            val left = InventoryRules.daysLeft(lot.expiryDate!!, today).toInt()
            val text = if (left == 0) stringResource(R.string.inventory_pill_expires_today)
            else pluralStringResource(R.plurals.inventory_pill_expires_in, left, left)
            LotPillData(text, LotPillKind.EXPIRING)
        }
        LotStatus.ACTIVE ->
            LotPillData(stringResource(R.string.inventory_pill_active), LotPillKind.ACTIVE)
    }
}
