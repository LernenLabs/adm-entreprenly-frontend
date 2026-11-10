package online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates

import java.math.BigDecimal
import java.time.Instant
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.PaymentStatus

data class SubscriptionPayment(val id: Long, val status: PaymentStatus, val amount: BigDecimal, val currency: String, val providerMessage: String?, val requestedAt: Instant)
