package online.entreprenly.entreprenlyapp.subscription.application.internal.commandservices

import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.subscription.application.commandservices.SubscriptionCommandService
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.CancelSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.CreateSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.PaySubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.ReactivateSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.RenewSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.SaveBillingCommand
import online.entreprenly.entreprenlyapp.subscription.domain.repositories.SubscriptionRepository

class SubscriptionCommandServiceImpl(private val repository: SubscriptionRepository) : SubscriptionCommandService {
    override suspend fun handle(command: SaveBillingCommand) = if (command.billing.invalidFields().isNotEmpty()) Result.Failure(ApplicationError.Validation("Invalid billing details")) else repository.saveBilling(command.userId, command.billing, command.method)
    override suspend fun handle(command: CreateSubscriptionCommand) = repository.create(command.userId, command.planId, command.token)
    override suspend fun handle(command: PaySubscriptionCommand) = repository.pay(command.id, command.token)
    override suspend fun handle(command: RenewSubscriptionCommand) = repository.renew(command.id, command.token)
    override suspend fun handle(command: CancelSubscriptionCommand) = repository.setCancellation(command.userId, true)
    override suspend fun handle(command: ReactivateSubscriptionCommand) = repository.setCancellation(command.userId, false)
}
