package online.entreprenly.entreprenlyapp.subscription.interfaces.rest.resources

import java.math.BigDecimal

data class SubscriptionDashboardResponse(
    val id: Long?,
    val defaultBillingCycle: String?,
    val currentPlan: DashboardPlanResource?,
    val recommendedPlan: DashboardPlanResource?,
    val limits: List<DashboardLimitResource>?,
    val billingSetup: DashboardBillingSetupResource?,
    val activity: List<DashboardActivityResource>?
)

data class DashboardPlanResource(
    val id: String?,
    val name: String?,
    val shortDescription: String?,
    val monthlyPrice: Double?,
    val annualPrice: Double?,
    val status: String?,
    val statusLabel: String?,
    val badgeLabel: String?,
    val recommended: Boolean?,
    val currentPeriodStartDate: String?,
    val currentPeriodEndDate: String?,
    val features: List<DashboardPlanFeatureResource>?
)

data class DashboardPlanFeatureResource(
    val description: String?,
    val available: Boolean?
)

data class DashboardLimitResource(
    val id: String?,
    val label: String?,
    val usedValue: Long?,
    val maxValue: Long?
)

data class DashboardBillingSetupResource(
    val paymentMethodTitle: String?,
    val paymentMethodDescription: String?,
    val paymentMethodActionLabel: String?,
    val fiscalDataTitle: String?,
    val fiscalDataDescription: String?,
    val fiscalDataActionLabel: String?,
    val hasPaymentMethod: Boolean?,
    val hasFiscalData: Boolean?,
    val paymentMethods: List<DashboardPaymentMethodResource>?,
    val fiscalData: DashboardFiscalDataResource?
)

data class DashboardPaymentMethodResource(
    val id: String?,
    val cardBrand: String?,
    val lastFour: String?,
    val holderName: String?,
    val expiryMonth: String?,
    val expiryYear: String?,
    val isDefault: Boolean?
)

data class DashboardFiscalDataResource(
    val documentType: String?,
    val documentNumber: String?,
    val businessName: String?,
    val receiptEmail: String?,
    val fiscalAddress: String?
)

data class DashboardActivityResource(
    val id: String?,
    val title: String?,
    val detail: String?
)

data class PlanResource(val id: Long, val code: String, val name: String, val amount: BigDecimal, val currency: String, val active: Boolean)
data class SubscriptionResource(val id: Long, val planId: Long, val status: String, val currentPeriodEnd: String?, val latestPaymentId: Long?)
data class PaymentResource(val id: Long, val status: String, val amount: BigDecimal, val currency: String, val providerMessage: String?, val requestedAt: String)
data class CreateSubscriptionResource(val userId: Long, val planId: Long, val cardToken: String, val paymentMethod: String = "FAKE_CARD", val billingPeriod: String = "MONTHLY")
data class PaymentRequest(val cardToken: String, val paymentMethod: String = "FAKE_CARD", val billingPeriod: String = "MONTHLY")
