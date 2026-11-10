package online.entreprenly.entreprenlyapp.subscription.domain.repositories

import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionDashboard
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPayment
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPlan
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentMethod

interface SubscriptionRepository {
    suspend fun plans(): Result<List<SubscriptionPlan>>
    suspend fun dashboard(userId: Long): Result<SubscriptionDashboard>
    suspend fun saveBilling(userId: Long, billing: BillingDetails, method: PaymentMethod? = null): Result<SubscriptionDashboard>
    suspend fun active(userId: Long): Result<Subscription?>
    suspend fun get(id: Long): Result<Subscription>
    suspend fun create(userId: Long, planId: Long, token: String): Result<Subscription>
    suspend fun pay(id: Long, token: String): Result<SubscriptionPayment>
    suspend fun renew(id: Long, token: String): Result<Subscription>
    suspend fun setCancellation(userId: Long, scheduled: Boolean): Result<SubscriptionDashboard>
    suspend fun payments(id: Long): Result<List<SubscriptionPayment>>
}
