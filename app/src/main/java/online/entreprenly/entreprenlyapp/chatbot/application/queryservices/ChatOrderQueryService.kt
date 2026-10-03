package online.entreprenly.entreprenlyapp.chatbot.application.queryservices

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetAllChatOrdersQuery
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ChatOrderQueryService {
    suspend fun handle(query: GetAllChatOrdersQuery): Result<List<ChatOrder>>
}
