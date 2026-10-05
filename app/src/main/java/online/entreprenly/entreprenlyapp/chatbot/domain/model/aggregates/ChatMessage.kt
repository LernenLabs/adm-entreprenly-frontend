package online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates

import java.time.Instant
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.MessageSender
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.MessageType

data class ChatMessage(
    val id: Long,
    val conversationId: Long,
    val content: String,
    val sender: MessageSender,
    val type: MessageType,
    val sentAt: Instant?
)
