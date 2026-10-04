package online.entreprenly.entreprenlyapp.chatbot.application.internal.commandservices

import online.entreprenly.entreprenlyapp.chatbot.application.commandservices.ChatMessageCommandService
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatMessage
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatMessageRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ChatMessageCommandServiceImpl(
    private val chatMessageRepository: ChatMessageRepository
) : ChatMessageCommandService {

    override suspend fun handle(command: SendChatMessageCommand): Result<ChatMessage> =
        chatMessageRepository.send(command.copy(content = command.content.trim()))
}
