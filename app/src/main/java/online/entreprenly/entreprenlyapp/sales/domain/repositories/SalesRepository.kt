package online.entreprenly.entreprenlyapp.sales.domain.repositories

import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import java.time.LocalDate
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.DailySalesSummary
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface SalesRepository {
    suspend fun createSale(sale: Sale): Result<Sale>
    suspend fun getDailySummary(date: LocalDate): Result<DailySalesSummary>
}
