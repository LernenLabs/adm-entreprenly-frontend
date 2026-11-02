package online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources

/** The backend recomputes the total and the subtotals, so they are not sent. */
data class CreateSaleRequest(
    val sellerId: Long,
    val paymentMethod: String,
    val status: String,
    val items: List<SaleItemResource>
)

/** Exactly one of [quantity] (sold by unit) and [weightKg] (sold by weight) is set. */
data class SaleItemResource(
    val productId: Long?,
    val productName: String,
    val quantity: Int?,
    val weightKg: Double?,
    val unitPrice: Double
)

data class SaleResource(
    val id: String,
    val paymentMethod: String,
    val total: Double,
    val timestamp: String
)

/** Backend payment methods: CASH, YAPE, PLIN, CARD. */
data class SaleSummaryResource(
    val total: Double,
    val paymentMethod: String?,
    val status: String?
)
