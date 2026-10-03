package online.entreprenly.entreprenlyapp.chatbot.application.queryservices

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllConversationsQuery
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ConversationQueryService {
    suspend fun handle(query: GetAllConversationsQuery): Result<List<Conversation>>
}
