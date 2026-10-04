package online.entreprenly.entreprenlyapp.chatbot.domain.repositories

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.Conversation
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ConversationRepository {
    suspend fun findAll(): Result<List<Conversation>>
}
