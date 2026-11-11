package online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.repositories

import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionDashboard
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentMethod
import online.entreprenly.entreprenlyapp.subscription.domain.repositories.SubscriptionRepository
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.api.SubscriptionApi
import online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.assemblers.toDomain
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.CreateSubscriptionResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.DashboardFiscalDataResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.DashboardPaymentMethodResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PaymentRequest
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PaymentResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PlanResource

class SubscriptionRepositoryImpl(private val api: SubscriptionApi) : SubscriptionRepository {
    override suspend fun plans() = safeApiCallWithBody({ api.plans() }) { it.map(PlanResource::toDomain) }
    override suspend fun dashboard(userId: Long) = safeApiCallWithBody({ api.dashboard(userId) }) { it.toDomain() }
    override suspend fun saveBilling(userId: Long, billing: BillingDetails, method: PaymentMethod?) =
        saveSetup(userId, billing, method, primaryId = method?.id)

    override suspend fun savePaymentMethod(userId: Long, method: PaymentMethod) =
        saveSetup(userId, null, method)

    override suspend fun setPrimaryPaymentMethod(userId: Long, methodId: String) =
        saveSetup(userId, null, null, primaryId = methodId)

    private suspend fun saveSetup(
        userId: Long,
        billing: BillingDetails?,
        method: PaymentMethod?,
        primaryId: String? = null
    ): Result<SubscriptionDashboard> {
        val current = safeApiCallWithBody({ api.dashboard(userId) }) { it }
        if (current is Result.Failure) return current
        val resource = (current as Result.Success).value
        val setup = resource.billingSetup ?: return Result.Failure(ApplicationError.Unexpected("Missing billing setup"))
        val fiscal = billing?.let {
            DashboardFiscalDataResource(if (it.documentNumber.length == 8) "DNI" else "RUC", it.documentNumber, it.businessName, it.receiptEmail, it.fiscalAddress)
        } ?: setup.fiscalData
        val saved = setup.paymentMethods.orEmpty()
        val methods = if (method == null) saved else saved + DashboardPaymentMethodResource(
            method.id, method.cardBrand, method.lastFour, method.holderName, method.expiryMonth, method.expiryYear, false
        )
        if (primaryId != null && methods.none { it.id == primaryId }) {
            return Result.Failure(ApplicationError.Validation("Payment method not found"))
        }
        val selectedId = primaryId ?: methods.firstOrNull { it.isDefault == true }?.id ?: methods.firstOrNull()?.id
        val updatedMethods = methods.map { it.copy(isDefault = selectedId != null && it.id == selectedId) }
        val updated = resource.copy(currentPlan = null, billingSetup = setup.copy(fiscalData = fiscal, hasFiscalData = if (billing != null) true else setup.hasFiscalData, paymentMethods = updatedMethods, hasPaymentMethod = updatedMethods.isNotEmpty()))
        return safeApiCallWithBody({ api.saveDashboard(userId, updated) }) { it.toDomain() }
    }
    override suspend fun active(userId: Long): Result<Subscription?> = when (val result = safeApiCallWithBody({ api.active(userId) }) { it.toDomain() }) {
        is Result.Success -> result
        is Result.Failure -> if (result.error is ApplicationError.NotFound) Result.Success(null) else result
    }
    override suspend fun get(id: Long) = safeApiCallWithBody({ api.get(id) }) { it.toDomain() }
    override suspend fun create(userId: Long, planId: Long, token: String): Result<Subscription> {
        val active = active(userId)
        if (active is Result.Failure) return active
        val existing = (active as Result.Success).value
        if (existing != null) {
            val catalog = plans()
            if (catalog is Result.Failure) return catalog
            val existingPlan = (catalog as Result.Success).value.firstOrNull { it.id == existing.planId }
            if (existingPlan?.code != "plan-free")
                return Result.Failure(ApplicationError.Conflict("An active paid subscription already exists"))
            // The current backend persists Free as ACTIVE and otherwise rejects an upgrade.
            // Close only that Free row at checkout; the dashboard still resolves Free until payment succeeds.
            val closed = safeApiCallWithBody({ api.cancel(existing.id) }) { it }
            if (closed is Result.Failure) return closed
        }
        return safeApiCallWithBody({ api.create(CreateSubscriptionResource(userId, planId, token)) }) { it.toDomain() }
    }
    override suspend fun pay(id: Long, token: String) = safeApiCallWithBody({ api.pay(id, PaymentRequest(token)) }) { it.toDomain() }
    override suspend fun renew(id: Long, token: String) = safeApiCallWithBody({ api.renew(id, PaymentRequest(token)) }) { it.toDomain() }
    override suspend fun setCancellation(userId: Long, scheduled: Boolean): Result<SubscriptionDashboard> {
        val current = safeApiCallWithBody({ api.dashboard(userId) }) { it }
        if (current is Result.Failure) return current
        val resource = (current as Result.Success).value
        val plan = resource.currentPlan
        if (plan?.id != "plan-control" || plan.status !in listOf("active", "scheduled-cancellation"))
            return Result.Failure(ApplicationError.Validation("No active Plan Control subscription"))
        val updated = resource.copy(currentPlan = plan.copy(status = if (scheduled) "scheduled-cancellation" else "active"))
        return safeApiCallWithBody({ api.saveDashboard(userId, updated) }) { it.toDomain() }
    }
    override suspend fun payments(id: Long) = safeApiCallWithBody({ api.payments(id) }) { it.map(PaymentResource::toDomain) }
}
