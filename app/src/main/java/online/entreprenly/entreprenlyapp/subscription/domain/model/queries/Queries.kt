package online.entreprenly.entreprenlyapp.subscription.domain.model.queries



data object GetSubscriptionPlansQuery
data class GetSubscriptionDashboardQuery(val userId: Long)
data class GetActiveSubscriptionQuery(val userId: Long)
data class GetSubscriptionQuery(val id: Long)
data class GetSubscriptionPaymentsQuery(val id: Long)
