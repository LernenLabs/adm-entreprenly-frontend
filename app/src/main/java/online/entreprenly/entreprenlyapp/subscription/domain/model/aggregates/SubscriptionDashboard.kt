package online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates

import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentMethod

data class SubscriptionDashboard(val planCode: String, val status: String, val endDate: String?, val billing: BillingDetails, val methods: List<PaymentMethod>) {
    val cancellationScheduled get() = status == "scheduled-cancellation"
}
