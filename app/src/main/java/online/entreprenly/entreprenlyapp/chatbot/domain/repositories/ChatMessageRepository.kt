package online.entreprenly.entreprenlyapp.chatbot.domain.repositories

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ChatMessageRepository {
    suspend fun findByConversationId(conversationId: Long): Result<List<ChatMessage>>
    suspend fun send(command: SendChatMessageCommand): Result<ChatMessage>
}
