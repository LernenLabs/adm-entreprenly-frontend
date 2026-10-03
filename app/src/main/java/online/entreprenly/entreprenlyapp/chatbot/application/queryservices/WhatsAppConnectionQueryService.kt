package online.entreprenly.entreprenlyapp.chatbot.application.queryservices

import online.entreprenly.entreprenlyapp.chatbot.domain.model.queries.GetWhatsAppConnectionQuery
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.WhatsAppConnection
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface WhatsAppConnectionQueryService {
    suspend fun handle(query: GetWhatsAppConnectionQuery): Result<WhatsAppConnection>
}
