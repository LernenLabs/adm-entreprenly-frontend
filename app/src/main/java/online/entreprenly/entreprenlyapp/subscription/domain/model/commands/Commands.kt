package online.entreprenly.entreprenlyapp.subscription.domain.model.commands

import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentMethod

data class SaveBillingCommand(val userId: Long, val billing: BillingDetails, val method: PaymentMethod? = null)
data class CreateSubscriptionCommand(val userId: Long, val planId: Long, val token: String)
data class PaySubscriptionCommand(val id: Long, val token: String)
data class RenewSubscriptionCommand(val id: Long, val token: String)
data class CancelSubscriptionCommand(val userId: Long)
data class ReactivateSubscriptionCommand(val userId: Long)
