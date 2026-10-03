package online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects

enum class MessageType(val value: String) {
    TEXT("text"),
    IMAGE("image");

    companion object {
        fun fromValue(value: String?): MessageType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: TEXT
    }
}
