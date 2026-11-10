package online.entreprenly.entreprenlyapp.profile.infrastructure.acl

import online.entreprenly.entreprenlyapp.profile.application.acl.SubscriptionPlanFacade
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.subscription.application.queryservices.SubscriptionQueryService
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionDashboardQuery
import online.entreprenly.entreprenlyapp.subscription.domain.model.queries.GetSubscriptionPlansQuery

/** Resolves the plan the same way the subscription panel does: dashboard plan code to plan name. */
class SubscriptionPlanFacadeImpl(
    private val subscriptionQueryService: SubscriptionQueryService
) : SubscriptionPlanFacade {

    override suspend fun currentPlanName(userId: Long): Result<String?> {
        val dashboard = when (val r = subscriptionQueryService.handle(GetSubscriptionDashboardQuery(userId))) {
            is Result.Success -> r.value
            is Result.Failure -> return r
        }
        return subscriptionQueryService.handle(GetSubscriptionPlansQuery).map { plans ->
            plans.firstOrNull { it.code == dashboard.planCode }?.name
        }
    }
}
