package online.entreprenly.entreprenlyapp.inventory.domain.model.entities

import java.time.Instant
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertSeverity
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertType
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType

/**
 * Stock alert from the backend. The backend regenerates ids on every query,
 * so identity is never persisted and navigation never uses [id].
 * The server-side english message is never shown: UI builds localized copy
 * from [alertType].
 */
data class StockAlert(
    val id: Long,
    val lotId: Long?,
    val productId: Long,
    val productType: ProductType?,
    val productName: String?,
    val alertType: AlertType?,
    val severity: AlertSeverity?,
    val createdAt: Instant?
)
