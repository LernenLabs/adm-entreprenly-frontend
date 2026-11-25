package online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources

// DTOs for the backend inventory endpoints. Dates are ISO-8601 UTC strings;
// assemblers convert them to Instant. Gson omits nulls, which fits the optionals.

// ---- Responses ----

data class UnitProductResource(
    val id: Long,
    val name: String,
    val description: String?,
    val codeQR: String?,
    val productType: String?,
    val price: Double,
    val weightGrams: Double,
    val brand: String?,
    val imageUrl: String? = null
)

data class WeightProductResource(
    val id: Long,
    val name: String,
    val description: String?,
    val codeQR: String?,
    val productType: String?,
    val pricePerKg: Double,
    val imageUrl: String? = null
)

data class UnitLotResource(
    val id: Long,
    val productId: Long,
    val codeQR: String?,
    val entryDate: String?,
    val lotType: String?,
    val quantity: Int,
    val expiryDate: String?
)

data class WeightLotResource(
    val id: Long,
    val productId: Long,
    val codeQR: String?,
    val entryDate: String?,
    val lotType: String?,
    val quantityKg: Double
)

/** Product data suggested for a package barcode. */
data class BarcodeLookupResource(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val description: String?,
    val imageUrl: String?,
    val source: String?
)

/** Path of an uploaded photo, relative to the API base URL. */
data class ProductImageResource(
    val imageUrl: String,
    val mimeType: String?,
    val sizeBytes: Long
)

data class StockAlertResource(
    val id: Long,
    val lotId: Long?,
    val productId: Long,
    val productType: String?,
    val productName: String?,
    val alertType: String?,
    val severity: String?,
    val message: String?,
    val createdAt: String?
)

// ---- Requests ----

data class CreateUnitProductResource(
    val name: String,
    val description: String?,
    val codeQR: String?,
    val price: Double,
    val weightGrams: Double,
    val brand: String?,
    val imageUrl: String? = null
)

data class UpdateUnitProductResource(
    val name: String,
    val description: String?,
    val codeQR: String?,
    val price: Double,
    val weightGrams: Double,
    val brand: String?,
    val imageUrl: String? = null
)

data class CreateWeightProductResource(
    val name: String,
    val description: String?,
    val codeQR: String?,
    val pricePerKg: Double,
    val imageUrl: String? = null
)

data class UpdateWeightProductResource(
    val name: String,
    val description: String?,
    val codeQR: String?,
    val pricePerKg: Double,
    val imageUrl: String? = null
)

data class CreateUnitLotResource(
    val productId: Long,
    val codeQR: String?,
    val entryDate: String?,
    val quantity: Int,
    val expiryDate: String?
)

data class UpdateUnitLotResource(
    val productId: Long,
    val codeQR: String?,
    val entryDate: String?,
    val quantity: Int,
    val expiryDate: String?
)

data class CreateWeightLotResource(
    val productId: Long,
    val codeQR: String?,
    val entryDate: String?,
    val quantityKg: Double
)

data class UpdateWeightLotResource(
    val productId: Long,
    val codeQR: String?,
    val entryDate: String?,
    val quantityKg: Double
)
