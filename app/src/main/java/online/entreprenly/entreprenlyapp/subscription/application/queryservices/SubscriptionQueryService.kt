package online.entreprenly.entreprenlyapp.subscription.application.queryservices

import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionDashboard
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPayment
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPlan
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetActiveSubscriptionQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionDashboardQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPaymentsQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPlansQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionQuery

interface SubscriptionQueryService {
    suspend fun handle(query: GetSubscriptionPlansQuery): Result<List<SubscriptionPlan>>
    suspend fun handle(query: GetSubscriptionDashboardQuery): Result<SubscriptionDashboard>
    suspend fun handle(query: GetActiveSubscriptionQuery): Result<Subscription?>
    suspend fun handle(query: GetSubscriptionQuery): Result<Subscription>
    suspend fun handle(query: GetSubscriptionPaymentsQuery): Result<List<SubscriptionPayment>>
}
