package online.entreprenly.entreprenlyapp.sales.application.queryservices

import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.CashRegisterSummary
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface SalesQueryService {
    suspend fun getCashRegisterSummary(): Result<CashRegisterSummary>
}
