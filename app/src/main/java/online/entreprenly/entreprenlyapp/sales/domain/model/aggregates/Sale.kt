package online.entreprenly.entreprenlyapp.sales.domain.model.aggregates

import online.entreprenly.entreprenlyapp.sales.domain.model.entities.SaleItem
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.PaymentMethod
import java.time.LocalDateTime
import java.util.UUID

data class Sale(
    val id: String = UUID.randomUUID().toString(),
    val items: List<SaleItem>,
    val paymentMethod: PaymentMethod,
    val total: Double,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
