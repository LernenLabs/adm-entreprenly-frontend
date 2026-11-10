package online.entreprenly.entreprenlyapp.inventory.domain.model.commands

import java.time.Instant
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType

/** Creates a product; initialStock is in units (UNIT) or kg (WEIGHT). */
data class CreateProductCommand(
    val name: String,
    val type: ProductType,
    val price: Double,
    val initialStock: Double
)

/** Full-replacement update: hidden fields must be re-sent (backend PUT semantics). */
data class UpdateProductCommand(
    val type: ProductType,
    val id: Long,
    val name: String,
    val price: Double,
    val description: String?,
    val codeQR: String?,
    val weightGrams: Double,
    val brand: String?
)

/** Creates a lot for an existing product. */
data class CreateLotCommand(
    val productType: ProductType,
    val productId: Long,
    val codeQR: String?,
    val entryDate: Instant,
    val expiryDate: Instant?,
    val quantityUnits: Int,
    val quantityKg: Double
)

/** Deletes a lot. Identity is (type, id), matching the backend routes. */
data class DeleteLotCommand(
    val type: ProductType,
    val id: Long
)

/** Deletes a product and its lots. Identity is (type, id), matching the backend routes. */
data class DeleteProductCommand(
    val type: ProductType,
    val id: Long
)
