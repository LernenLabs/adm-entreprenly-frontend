package online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates

import java.time.Instant
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType

/**
 * Stock lot. Identity is the pair (type, id). A lot links to its product via
 * (type, productId). Only UNIT lots carry an expiry date.
 *
 * quantityUnits is meaningful for UNIT lots, quantityKg for WEIGHT lots.
 */
data class Lot(
    val type: ProductType,
    val id: Long,
    val productId: Long,
    val codeQR: String?,
    val entryDate: Instant,
    val expiryDate: Instant?,
    val quantityUnits: Int,
    val quantityKg: Double
) {
    val key: String get() = "${type.value}/$id"

    /** Amount in the product's own unit (units or kg). */
    val amount: Double get() = if (type == ProductType.UNIT) quantityUnits.toDouble() else quantityKg

    val isEmpty: Boolean get() = amount <= 0.0

    /** UI label: codeQR when present, otherwise L-%04d. */
    val label: String get() =
        if (!codeQR.isNullOrBlank()) codeQR else "L-%04d".format(id)
}
