package online.entreprenly.entreprenlyapp.sales.application.internal.commandservices

import online.entreprenly.entreprenlyapp.sales.application.commandservices.SalesCommandService
import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import online.entreprenly.entreprenlyapp.sales.domain.repositories.SalesRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class SalesCommandServiceImpl(
    private val salesRepository: SalesRepository
) : SalesCommandService {
    override suspend fun createSale(sale: Sale): Result<Sale> {
        return salesRepository.createSale(sale)
    }
}
