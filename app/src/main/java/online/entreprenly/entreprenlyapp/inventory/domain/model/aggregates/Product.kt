package online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates

import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType

/**
 * Catalog product. Identity is the pair (type, id): a UnitProduct#1 and a
 * WeightProduct#1 can exist at the same time.
 *
 * price holds the unit price for UNIT products and the price per kg for
 * WEIGHT products. weightGrams and brand only apply to UNIT products.
 */
data class Product(
    val type: ProductType,
    val id: Long,
    val name: String,
    val description: String?,
    val codeQR: String?,
    val price: Double,
    val weightGrams: Double,
    val brand: String?
) {
    val key: String get() = "${type.value}/$id"
}
