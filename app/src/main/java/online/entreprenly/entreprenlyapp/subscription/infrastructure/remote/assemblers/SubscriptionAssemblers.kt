package online.entreprenly.entreprenlyapp.subscription.infrastructure.remote.assemblers

import java.time.Instant
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionDashboard
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPayment
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPlan
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentMethod
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentStatus
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.SubscriptionStatus
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PaymentResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.PlanResource
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.SubscriptionDashboardResponse
import online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources.SubscriptionResource

fun PlanResource.toDomain() = SubscriptionPlan(id, code, name, amount, currency, active)
fun SubscriptionResource.toDomain() = Subscription(id, planId, SubscriptionStatus.valueOf(status), currentPeriodEnd?.let(Instant::parse), latestPaymentId)
fun PaymentResource.toDomain() = SubscriptionPayment(id, PaymentStatus.valueOf(status), amount, currency, providerMessage, Instant.parse(requestedAt))
fun SubscriptionDashboardResponse.toDomain(): SubscriptionDashboard {
    val fiscal = billingSetup?.fiscalData
    return SubscriptionDashboard(
        requireNotNull(currentPlan?.id), requireNotNull(currentPlan?.status), currentPlan.currentPeriodEndDate,
        BillingDetails(fiscal?.businessName.orEmpty(), fiscal?.documentNumber.orEmpty(), fiscal?.fiscalAddress.orEmpty(), fiscal?.receiptEmail.orEmpty()),
        billingSetup?.paymentMethods.orEmpty().map { PaymentMethod(requireNotNull(it.id), it.cardBrand.orEmpty(), it.lastFour.orEmpty(), it.holderName.orEmpty(), it.expiryMonth.orEmpty(), it.expiryYear.orEmpty(), it.isDefault == true) }
    )
}
