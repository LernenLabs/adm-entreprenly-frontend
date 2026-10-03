package online.entreprenly.entreprenlyapp.chatbot.infrastructure.acl

import kotlinx.coroutines.flow.first
import online.entreprenly.entreprenlyapp.chatbot.application.acl.SubscriptionAccessFacade
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Backend subscription endpoint the temporary facade calls (owned by the subscription context). */
interface SubscriptionAccessApi {
    @GET("api/v1/subscriptions/access")
    suspend fun validateAccess(
        @Query("userId") userId: Long,
        @Query("feature") feature: String
    ): Response<AccessValidationResource>
}

data class AccessValidationResource(val userId: Long?, val feature: String?, val hasAccess: Boolean)

/**
 * Temporary implementation until the subscription context exposes its own facade:
 * asks the backend whether the signed-in user's plan includes the chatbot feature.
 */
class SubscriptionAccessFacadeImpl(
    private val api: SubscriptionAccessApi,
    private val sessionQueryService: SessionQueryService
) : SubscriptionAccessFacade {

    override suspend fun hasChatbotAccess(): Result<Boolean> {
        val userId = sessionQueryService.handle(GetCurrentSessionQuery).first()?.userId
            ?: return Result.Failure(ApplicationError.Unauthorized("No active session"))
        return safeApiCallWithBody({ api.validateAccess(userId, CHATBOT_FEATURE) }) { it.hasAccess }
    }

    private companion object {
        const val CHATBOT_FEATURE = "chatbot"
    }
}
