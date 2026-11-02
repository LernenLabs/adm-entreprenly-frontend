package online.entreprenly.entreprenlyapp.sales.application.queryservices

import java.time.LocalDate
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.CashRegisterSummary
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.DailySalesSummary
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface SalesQueryService {
    suspend fun getCashRegisterSummary(): Result<CashRegisterSummary>
    suspend fun getDailySummary(date: LocalDate): Result<DailySalesSummary>
}
