package online.entreprenly.entreprenlyapp.profile.application.acl

import online.entreprenly.entreprenlyapp.shared.application.result.Result

/**
 * What the profile needs from the subscription context: the name of the user's current plan.
 * The profile endpoint's own `plan` field is not updated when the user subscribes, so the
 * subscription context is the source of truth. A null name means the plan could not be resolved.
 */
interface SubscriptionPlanFacade {
    suspend fun currentPlanName(userId: Long): Result<String?>
}
