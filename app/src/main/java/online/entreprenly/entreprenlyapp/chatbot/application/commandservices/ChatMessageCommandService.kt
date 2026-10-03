package online.entreprenly.entreprenlyapp.chatbot.application.commandservices

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ChatMessageCommandService {
    suspend fun handle(command: SendChatMessageCommand): Result<ChatMessage>
}
