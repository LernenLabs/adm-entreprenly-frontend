package online.entreprenly.entreprenlyapp.chatbot.application.queryservices

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetChatMessagesByConversationIdQuery
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ChatMessageQueryService {
    suspend fun handle(query: GetChatMessagesByConversationIdQuery): Result<List<ChatMessage>>
}
