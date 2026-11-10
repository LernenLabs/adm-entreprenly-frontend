package online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects

/** What was actually sold in one business day (cancelled sales excluded). */
data class DailySalesSummary(
    val count: Int,
    val cashTotal: Double,
    val digitalTotal: Double
) {
    val total: Double get() = cashTotal + digitalTotal
}
