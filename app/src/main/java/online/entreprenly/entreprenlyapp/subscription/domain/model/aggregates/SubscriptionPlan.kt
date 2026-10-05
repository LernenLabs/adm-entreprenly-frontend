package online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates

import java.math.BigDecimal

data class SubscriptionPlan(val id: Long, val code: String, val name: String, val amount: BigDecimal, val currency: String, val active: Boolean)
