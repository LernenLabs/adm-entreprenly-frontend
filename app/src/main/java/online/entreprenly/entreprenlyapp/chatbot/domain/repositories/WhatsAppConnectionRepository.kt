package online.entreprenly.entreprenlyapp.chatbot.domain.repositories

import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface WhatsAppConnectionRepository {
    suspend fun current(): Result<WhatsAppConnection>
}
