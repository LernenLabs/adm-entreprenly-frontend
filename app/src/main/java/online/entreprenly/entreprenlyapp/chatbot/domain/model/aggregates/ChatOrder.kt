package online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates

import java.time.Instant
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderItem
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus

/** Order captured through a WhatsApp conversation. [receiptImage] is a data URL. */
data class ChatOrder(
    val id: Long,
    val conversationId: Long,
    val orderNumber: String,
    val items: List<OrderItem>,
    val total: Double,
    val deliveryAddress: String?,
    val paymentMethod: String?,
    val status: OrderStatus,
    val hasReceipt: Boolean,
    val rejectionCount: Int,
    val createdAt: Instant?,
    val receiptImage: String?
) {
    /** The client sent a receipt that the seller has not approved or rejected yet. */
    val awaitingValidation: Boolean get() = status == OrderStatus.WAITING_PAYMENT && hasReceipt
}
