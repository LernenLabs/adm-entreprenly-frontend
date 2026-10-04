package online.entreprenly.entreprenlyapp.chatbot.domain.model.commands

/** The seller writes into a conversation; the message is sent as the bot. */
data class SendChatMessageCommand(val conversationId: Long, val content: String)

/** Accepts the receipt: the backend deducts stock and registers the sale. */
data class ApproveOrderPaymentCommand(val orderId: Long)

/** Rejects the receipt and sends [message] (the reason) to the client through the bot. */
data class RejectOrderPaymentCommand(val orderId: Long, val conversationId: Long, val message: String)
