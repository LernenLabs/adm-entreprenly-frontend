package online.entreprenly.entreprenlyapp.chatbot.application.internal.commandservices

import online.entreprenly.entreprenlyapp.chatbot.application.commandservices.ChatOrderCommandService
import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.ApproveOrderPaymentCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.RejectOrderPaymentCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.SendChatMessageCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.valueobjects.OrderStatus
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatMessageRepository
import online.entreprenly.entreprenlyapp.chatbot.domain.repositories.ChatOrderRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ChatOrderCommandServiceImpl(
    private val chatOrderRepository: ChatOrderRepository,
    private val chatMessageRepository: ChatMessageRepository
) : ChatOrderCommandService {

    /** Stock deduction and sale registration happen in the backend on CONFIRMED. */
    override suspend fun handle(command: ApproveOrderPaymentCommand): Result<ChatOrder> =
        chatOrderRepository.updateStatus(command.orderId, OrderStatus.CONFIRMED, hasReceipt = true)
            .also { notifyClient(it, command.conversationId, command.message) }

    /** The backend counts the rejection (and blocks the order after too many). */
    override suspend fun handle(command: RejectOrderPaymentCommand): Result<ChatOrder> =
        chatOrderRepository.updateStatus(command.orderId, OrderStatus.WAITING_PAYMENT, hasReceipt = false)
            .also { notifyClient(it, command.conversationId, command.message) }

    /**
     * Sends [message] as a bot message once the status change succeeded; the backend relays bot
     * messages to the client's WhatsApp. Best effort: the decision stands even if it fails.
     */
    private suspend fun notifyClient(result: Result<ChatOrder>, conversationId: Long, message: String) {
        if (result is Result.Success && message.isNotBlank()) {
            chatMessageRepository.send(SendChatMessageCommand(conversationId, message))
        }
    }
}
