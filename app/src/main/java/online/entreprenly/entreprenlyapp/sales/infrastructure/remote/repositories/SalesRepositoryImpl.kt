package online.entreprenly.entreprenlyapp.sales.infrastructure.remote.repositories

import java.time.LocalDate
import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.DailySalesSummary
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.PaymentMethod
import online.entreprenly.entreprenlyapp.sales.domain.repositories.SalesRepository
import online.entreprenly.entreprenlyapp.sales.infrastructure.remote.api.SalesApi
import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.CreateSaleRequest
import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.SaleItemResource
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCall
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody

class SalesRepositoryImpl(
    private val api: SalesApi,
    private val currentUserId: suspend () -> Long?
) : SalesRepository {

    /** A sale only counts once the backend saved it: failures reach the seller, ticket intact. */
    override suspend fun createSale(sale: Sale): Result<Sale> {
        val sellerId = currentUserId()
            ?: return Result.Failure(ApplicationError.Unauthorized("No active session"))
        val request = CreateSaleRequest(
            sellerId = sellerId,
            paymentMethod = sale.paymentMethod.toBackendValue(),
            status = STATUS_COMPLETED,
            items = sale.items.map {
                SaleItemResource(
                    productId = it.productId.toLongOrNull(),
                    productName = it.productName,
                    quantity = if (it.isUnit) it.quantity.toInt() else null,
                    weightKg = if (it.isUnit) null else it.quantity,
                    unitPrice = it.unitPrice,
                    subtotal = it.subtotal
                )
            }
        )
        return safeApiCallWithBody({ api.createSale(request) }) { response -> sale.copy(id = response.id) }
    }

    override suspend fun getDailySummary(date: LocalDate): Result<DailySalesSummary> =
        safeApiCall({ api.getSales(date.toString()) }) { body ->
            val sales = body.orEmpty().filterNot { it.status.equals("CANCELLED", ignoreCase = true) }
            val (cash, digital) = sales.partition { it.paymentMethod.equals("CASH", ignoreCase = true) }
            DailySalesSummary(sales.size, cash.sumOf { it.total }, digital.sumOf { it.total })
        }

    /** The backend has no generic "digital" method; the POS groups Yape, Plin and card under it. */
    private fun PaymentMethod.toBackendValue(): String = when (this) {
        PaymentMethod.CASH -> "CASH"
        PaymentMethod.DIGITAL -> "YAPE"
    }

    private companion object {
        const val STATUS_COMPLETED = "COMPLETED"
    }
}
