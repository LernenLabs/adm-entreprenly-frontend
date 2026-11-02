package online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources

data class CreateSaleRequest(
    val paymentMethod: String,
    val total: Double,
    val items: List<SaleItemResource>
)

data class SaleItemResource(
    val productId: String,
    val quantity: Double,
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

data class CashRegisterResource(
    val cashTotal: Double,
    val digitalTotal: Double
)
