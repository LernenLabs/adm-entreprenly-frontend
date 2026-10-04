package online.entreprenly.entreprenlyapp.sales.domain.repositories

import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.CashRegisterSummary
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface SalesRepository {
    suspend fun createSale(sale: Sale): Result<Sale>
    suspend fun getCashRegisterSummary(): Result<CashRegisterSummary>
}
