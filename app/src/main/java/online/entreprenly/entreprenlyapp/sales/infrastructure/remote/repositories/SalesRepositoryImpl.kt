package online.entreprenly.entreprenlyapp.sales.infrastructure.remote.repositories

import kotlinx.coroutines.delay
import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.CashRegisterSummary
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.PaymentMethod
import online.entreprenly.entreprenlyapp.sales.domain.repositories.SalesRepository
import online.entreprenly.entreprenlyapp.sales.infrastructure.remote.api.SalesApi
import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.CreateSaleRequest
import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.SaleItemResource
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody

class SalesRepositoryImpl(
    private val api: SalesApi
) : SalesRepository {

    // --- Mocks: Simulan los datos en caso la API no esté disponible o haya problemas de red.
    private var mockCashTotal = 150.50
    private var mockDigitalTotal = 320.00
    private val mockSales = mutableListOf<Sale>()

    override suspend fun createSale(sale: Sale): Result<Sale> {
        val request = CreateSaleRequest(
            paymentMethod = sale.paymentMethod.name,
            total = sale.total,
            items = sale.items.map {
                SaleItemResource(
                    productId = it.productId,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice
                )
            }
        )

        val apiResult = safeApiCallWithBody(
            call = { api.createSale(request) },
            transform = { response -> 
                // En un escenario real, reconstruimos la entidad a partir de los datos recibidos.
                sale.copy(id = response.id) 
            }
        )

        return when (apiResult) {
            is Result.Success -> apiResult
            is Result.Failure -> {
                // FALLBACK: Simulamos un retardo de red de 800ms
                delay(800)
                mockSales.add(sale)
                if (sale.paymentMethod == PaymentMethod.CASH) {
                    mockCashTotal += sale.total
                } else {
                    mockDigitalTotal += sale.total
                }
                // Como es offline/fallback, devolvemos el Sale generado localmente (que ya tiene UUID)
                Result.Success(sale) 
            }
        }
    }

    override suspend fun getCashRegisterSummary(): Result<CashRegisterSummary> {
        val apiResult = safeApiCallWithBody(
            call = { api.getCashRegisters() },
            transform = { response ->
                CashRegisterSummary(
                    cashTotal = response.cashTotal,
                    digitalTotal = response.digitalTotal
                )
            }
        )

        return when (apiResult) {
            is Result.Success -> apiResult
            is Result.Failure -> {
                // FALLBACK: Simulamos un retardo de red de 500ms
                delay(500)
                Result.Success(CashRegisterSummary(mockCashTotal, mockDigitalTotal))
            }
        }
    }
}
