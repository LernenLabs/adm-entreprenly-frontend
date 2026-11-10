package online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects



enum class SubscriptionStatus { ACTIVE, PENDING_PAYMENT, CANCELLED, EXPIRED, SUSPENDED }
enum class PaymentStatus { PENDING, APPROVED, DECLINED, FAILED }
