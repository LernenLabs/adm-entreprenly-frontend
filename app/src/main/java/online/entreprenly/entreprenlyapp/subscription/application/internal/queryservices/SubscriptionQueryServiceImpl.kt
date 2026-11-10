package online.entreprenly.entreprenlyapp.subscription.application.internal.queryservices

import online.entreprenly.entreprenlyapp.subscription.application.queryservices.SubscriptionQueryService
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetActiveSubscriptionQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionDashboardQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPaymentsQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPlansQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionQuery
import online.entreprenly.entreprenlyapp.subscription.domain.repositories.SubscriptionRepository

class SubscriptionQueryServiceImpl(private val repository: SubscriptionRepository) : SubscriptionQueryService {
    override suspend fun handle(query: GetSubscriptionPlansQuery) = repository.plans()
    override suspend fun handle(query: GetSubscriptionDashboardQuery) = repository.dashboard(query.userId)
    override suspend fun handle(query: GetActiveSubscriptionQuery) = repository.active(query.userId)
    override suspend fun handle(query: GetSubscriptionQuery) = repository.get(query.id)
    override suspend fun handle(query: GetSubscriptionPaymentsQuery) = repository.payments(query.id)
}
