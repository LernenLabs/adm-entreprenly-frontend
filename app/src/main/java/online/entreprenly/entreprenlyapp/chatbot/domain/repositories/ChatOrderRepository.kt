package online.entreprenly.entreprenlyapp.chatbot.domain.repositories

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ChatOrderRepository {
    suspend fun findAll(): Result<List<ChatOrder>>
    suspend fun updateStatus(orderId: Long, status: OrderStatus, hasReceipt: Boolean): Result<ChatOrder>
}
