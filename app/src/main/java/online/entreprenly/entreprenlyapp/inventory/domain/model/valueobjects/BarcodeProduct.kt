package online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects

/**
 * Product data suggested for a package barcode (from a public product database). Any field
 * may be null; the seller still sets the price and the stock.
 */
data class BarcodeProduct(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val description: String?,
    val imageUrl: String?
)
