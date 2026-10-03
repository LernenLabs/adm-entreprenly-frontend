package online.entreprenly.entreprenlyapp.chatbot.application.commandservices

import online.entreprenly.entreprenlyapp.chatbot.domain.model.aggregates.ChatOrder
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.ApproveOrderPaymentCommand
import online.entreprenly.entreprenlyapp.chatbot.domain.model.commands.RejectOrderPaymentCommand
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ChatOrderCommandService {
    suspend fun handle(command: ApproveOrderPaymentCommand): Result<ChatOrder>
    suspend fun handle(command: RejectOrderPaymentCommand): Result<ChatOrder>
}
