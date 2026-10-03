package online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources

// DTOs for the backend chatbot endpoints (api/v1/conversations, chat-messages, chat-orders,
// chatbot/whatsapp/bridge). Fields are nullable because Gson ignores Kotlin nullability.

data class ConversationResource(
    val id: Long,
    val clientName: String?,
    val clientPhone: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val createdAt: String?
)

data class ChatMessageResource(
    val id: Long,
    val conversationId: Long,
    val content: String?,
    val sender: String?,
    val type: String?,
    val sentAt: String?
)

data class CreateChatMessageResource(
    val conversationId: Long,
    val content: String,
    val sender: String
)

data class OrderItemResource(val productName: String?, val quantity: Int, val unitPrice: Double)

data class ChatOrderResource(
    val id: Long,
    val conversationId: Long,
    val orderNumber: String?,
    val items: List<OrderItemResource>?,
    val total: Double,
    val deliveryAddress: String?,
    val paymentMethod: String?,
    val status: String?,
    val hasReceipt: Boolean,
    val rejectionCount: Int,
    val createdAt: String?,
    val receiptImage: String?
)

data class UpdateChatOrderResource(val status: String, val hasReceipt: Boolean)

data class BridgeQrStateResource(val qr: String?, val connected: Boolean)
