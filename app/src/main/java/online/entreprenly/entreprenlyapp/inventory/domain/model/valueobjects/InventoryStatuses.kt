package online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects

/** Derived client-side status of a lot, in priority order. */
enum class LotStatus {
    OUT_OF_STOCK,
    EXPIRED,
    EXPIRING_SOON,
    ACTIVE
}

/** Backend alert types for inventory-stock-alerts. */
enum class AlertType(val value: String) {
    EXPIRED("expired"),
    OUT_OF_STOCK("out_of_stock"),
    EXPIRING_SOON("expiring_soon"),
    LOW_STOCK("low_stock");

    companion object {
        fun fromValue(value: String?): AlertType? =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
    }
}

/** Backend alert severity. */
enum class AlertSeverity(val value: String) {
    WARNING("warning"),
    CRITICAL("critical");

    companion object {
        fun fromValue(value: String?): AlertSeverity? =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
    }
}
