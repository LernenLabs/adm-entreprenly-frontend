package online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates

import java.time.Instant

/** WhatsApp conversation between the chatbot and a client. */
data class Conversation(
    val id: Long,
    val clientName: String?,
    val clientPhone: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val createdAt: Instant?
) {
    /** Name to show for the client, falling back to the phone number. */
    val displayName: String? get() = clientName?.takeIf { it.isNotBlank() } ?: clientPhone
}
