package online.entreprenly.entreprenlyapp.sales.domain.model.entities

data class SaleItem(
    val productId: String,
    val productName: String,
    val isUnit: Boolean, // true = Por unidad, false = Por peso
    val quantity: Double,
    val unitPrice: Double
) {
    val subtotal: Double
        get() = quantity * unitPrice
}

// Clase auxiliar para el buscador y selección
data class SaleProduct(
    val id: String,
    val name: String,
    val isUnit: Boolean,
    val price: Double,
    val stockAvailable: Double
)
