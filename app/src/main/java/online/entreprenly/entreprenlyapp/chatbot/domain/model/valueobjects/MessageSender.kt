package online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects

/** Who authored a chat message; [value] is the backend's wire format. */
enum class MessageSender(val value: String) {
    CLIENT("client"),
    BOT("bot"),
    SYSTEM("system");

    companion object {
        fun fromValue(value: String?): MessageSender =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: SYSTEM
    }
}
