package online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.assemblers

import java.time.Instant
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.MessageSender
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.MessageType
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderItem
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.BridgeQrStateResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.ChatMessageResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.ChatOrderResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.ConversationResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.CreateChatMessageResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.OrderItemResource
import online.entreprenly.entreprenlyapp.chatbot.interfaces.rest.resources.UpdateChatOrderResource

/** Backend instants are ISO-8601 strings. */
private fun String?.toInstantOrNull(): Instant? = this?.let { runCatching { Instant.parse(it) }.getOrNull() }

fun ConversationResource.toEntity() = Conversation(
    id = id,
    clientName = clientName,
    clientPhone = clientPhone,
    lastMessage = lastMessage,
    lastMessageTime = lastMessageTime,
    createdAt = createdAt.toInstantOrNull()
)

fun ChatMessageResource.toEntity() = ChatMessage(
    id = id,
    conversationId = conversationId,
    content = content.orEmpty(),
    sender = MessageSender.fromValue(sender),
    type = MessageType.fromValue(type),
    sentAt = sentAt.toInstantOrNull()
)

/** Messages written from the app are sent as the bot. */
fun SendChatMessageCommand.toResource() =
    CreateChatMessageResource(conversationId, content, MessageSender.BOT.value)

fun OrderItemResource.toValueObject() = OrderItem(productName.orEmpty(), quantity, unitPrice)

fun ChatOrderResource.toEntity() = ChatOrder(
    id = id,
    conversationId = conversationId,
    orderNumber = orderNumber ?: "#$id",
    items = items.orEmpty().map { it.toValueObject() },
    total = total,
    deliveryAddress = deliveryAddress,
    paymentMethod = paymentMethod,
    status = OrderStatus.fromName(status),
    hasReceipt = hasReceipt,
    rejectionCount = rejectionCount,
    createdAt = createdAt.toInstantOrNull(),
    receiptImage = receiptImage
)

fun OrderStatus.toUpdateResource(hasReceipt: Boolean) = UpdateChatOrderResource(name, hasReceipt)

fun BridgeQrStateResource.toValueObject() = WhatsAppConnection(connected = connected, qr = qr)
