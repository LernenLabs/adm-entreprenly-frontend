package online.entreprenly.entreprenlyapp.sales.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.CashRegisterResource
import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.CreateSaleRequest
import online.entreprenly.entreprenlyapp.sales.interfaces.rest.resources.SaleResource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SalesApi {
    @POST("api/v1/sales")
    suspend fun createSale(@Body request: CreateSaleRequest): Response<SaleResource>

    @GET("api/v1/cash-registers")
    suspend fun getCashRegisters(): Response<CashRegisterResource>
}
