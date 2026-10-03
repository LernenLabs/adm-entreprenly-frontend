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

    /** The backend counts the rejection (and blocks the order after too many). */
    override suspend fun handle(command: RejectOrderPaymentCommand): Result<ChatOrder> =
        chatOrderRepository.updateStatus(command.orderId, OrderStatus.WAITING_PAYMENT, hasReceipt = false)
            .also {
                // Best effort: the rejection already happened even if the reason fails to send.
                if (it is Result.Success && command.message.isNotBlank()) {
                    chatMessageRepository.send(SendChatMessageCommand(command.conversationId, command.message))
                }
            }
}
