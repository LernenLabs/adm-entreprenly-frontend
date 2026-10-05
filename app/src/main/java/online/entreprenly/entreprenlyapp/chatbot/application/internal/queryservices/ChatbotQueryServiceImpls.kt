package online.entreprenly.entreprenlyapp.chatbot.application.internal.queryservices

import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatMessageQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ChatOrderQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.ConversationQueryService
import online.entreprenly.entreprenlyapp.chatbot.application.queryservices.WhatsAppConnectionQueryService
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllChatOrdersQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllConversationsQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetChatMessagesByConversationIdQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetWhatsAppConnectionQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatMessageRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatOrderRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ConversationRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.WhatsAppConnectionRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ConversationQueryServiceImpl(
    private val conversationRepository: ConversationRepository
) : ConversationQueryService {
    override suspend fun handle(query: GetAllConversationsQuery): Result<List<Conversation>> =
        conversationRepository.findAll()
}

class ChatMessageQueryServiceImpl(
    private val chatMessageRepository: ChatMessageRepository
) : ChatMessageQueryService {
    /** Oldest first, as a chat reads. */
    override suspend fun handle(query: GetChatMessagesByConversationIdQuery): Result<List<ChatMessage>> =
        chatMessageRepository.findByConversationId(query.conversationId)
            .map { messages -> messages.sortedWith(compareBy({ it.sentAt }, { it.id })) }
}

class ChatOrderQueryServiceImpl(
    private val chatOrderRepository: ChatOrderRepository
) : ChatOrderQueryService {
    /** Newest first. */
    override suspend fun handle(query: GetAllChatOrdersQuery): Result<List<ChatOrder>> =
        chatOrderRepository.findAll()
            .map { orders -> orders.sortedWith(compareByDescending<ChatOrder> { it.createdAt }.thenByDescending { it.id }) }
}

class WhatsAppConnectionQueryServiceImpl(
    private val whatsAppConnectionRepository: WhatsAppConnectionRepository
) : WhatsAppConnectionQueryService {
    override suspend fun handle(query: GetWhatsAppConnectionQuery): Result<WhatsAppConnection> =
        whatsAppConnectionRepository.current()
}
