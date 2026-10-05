package online.entreprenly.entreprenlyapp.sales.application.internal.queryservices

import online.entreprenly.entreprenlyapp.sales.application.queryservices.SalesQueryService
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.CashRegisterSummary
import online.entreprenly.entreprenlyapp.sales.domain.repositories.SalesRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class SalesQueryServiceImpl(
    private val salesRepository: SalesRepository
) : SalesQueryService {
    override suspend fun getCashRegisterSummary(): Result<CashRegisterSummary> {
        return salesRepository.getCashRegisterSummary()
    }
}
