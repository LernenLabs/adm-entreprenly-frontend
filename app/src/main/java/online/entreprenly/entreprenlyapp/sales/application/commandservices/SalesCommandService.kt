package online.entreprenly.entreprenlyapp.sales.application.commandservices

import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface SalesCommandService {
    suspend fun createSale(sale: Sale): Result<Sale>
}
