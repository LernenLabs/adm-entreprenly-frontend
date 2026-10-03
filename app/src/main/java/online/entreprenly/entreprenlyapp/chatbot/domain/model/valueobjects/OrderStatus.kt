package online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects

/** Lifecycle of an order captured by the chatbot (mirrors the backend enum). */
enum class OrderStatus {
    PENDING,
    WAITING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    BLOCKED;

    companion object {
        fun fromName(name: String?): OrderStatus =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PENDING
    }
}
