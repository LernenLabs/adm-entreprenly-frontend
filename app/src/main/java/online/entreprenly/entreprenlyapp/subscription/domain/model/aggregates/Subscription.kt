package online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates

import java.time.Instant
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.SubscriptionStatus

data class Subscription(val id: Long, val planId: Long, val status: SubscriptionStatus, val currentPeriodEnd: Instant?, val latestPaymentId: Long?) {
    fun isActiveAt(now: Instant) = status == SubscriptionStatus.ACTIVE && currentPeriodEnd?.isAfter(now) == true
}
