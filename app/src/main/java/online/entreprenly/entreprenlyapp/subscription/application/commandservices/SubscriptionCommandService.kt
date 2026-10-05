package online.entreprenly.entreprenlyapp.subscription.application.commandservices

import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionDashboard
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPayment
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.CancelSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.CreateSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.PaySubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.ReactivateSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.RenewSubscriptionCommand
import online.entreprenly.entreprenlyapp.subscription.domain.model.commands.SaveBillingCommand

interface SubscriptionCommandService {
    suspend fun handle(command: SaveBillingCommand): Result<SubscriptionDashboard>
    suspend fun handle(command: CreateSubscriptionCommand): Result<Subscription>
    suspend fun handle(command: PaySubscriptionCommand): Result<SubscriptionPayment>
    suspend fun handle(command: RenewSubscriptionCommand): Result<Subscription>
    suspend fun handle(command: CancelSubscriptionCommand): Result<SubscriptionDashboard>
    suspend fun handle(command: ReactivateSubscriptionCommand): Result<SubscriptionDashboard>
}
