package online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.repositories

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatMessageRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatOrderRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ConversationRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.WhatsAppConnectionRepository
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.api.ChatbotApi
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.assemblers.toEntity
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.assemblers.toResource
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.assemblers.toUpdateResource
import online.entreprenly.entreprenlyapp.chatbot.infrastructure.remote.assemblers.toValueObject
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCall
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody

class ConversationRepositoryImpl(private val api: ChatbotApi) : ConversationRepository {
    override suspend fun findAll(): Result<List<Conversation>> =
        safeApiCall({ api.getConversations() }) { body -> body.orEmpty().map { it.toEntity() } }
}

class ChatMessageRepositoryImpl(private val api: ChatbotApi) : ChatMessageRepository {
    override suspend fun findByConversationId(conversationId: Long): Result<List<ChatMessage>> =
        safeApiCall({ api.getMessages(conversationId) }) { body -> body.orEmpty().map { it.toEntity() } }

    override suspend fun send(command: SendChatMessageCommand): Result<ChatMessage> =
        safeApiCallWithBody({ api.createMessage(command.toResource()) }) { it.toEntity() }
}

class ChatOrderRepositoryImpl(private val api: ChatbotApi) : ChatOrderRepository {
    override suspend fun findAll(): Result<List<ChatOrder>> =
        safeApiCall({ api.getOrders() }) { body -> body.orEmpty().map { it.toEntity() } }

    override suspend fun updateStatus(orderId: Long, status: OrderStatus, hasReceipt: Boolean): Result<ChatOrder> =
        safeApiCallWithBody({ api.updateOrder(orderId, status.toUpdateResource(hasReceipt)) }) { it.toEntity() }
}

class WhatsAppConnectionRepositoryImpl(private val api: ChatbotApi) : WhatsAppConnectionRepository {
    override suspend fun current(): Result<WhatsAppConnection> =
        safeApiCallWithBody({ api.getBridgeQr() }) { it.toValueObject() }
}
