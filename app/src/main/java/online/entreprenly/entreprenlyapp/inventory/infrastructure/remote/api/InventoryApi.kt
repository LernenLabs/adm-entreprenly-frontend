package online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.api

import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateUnitLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateUnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateWeightLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.CreateWeightProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.StockAlertResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UnitLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UpdateUnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UpdateWeightProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.WeightLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.WeightProductResource
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Backend inventory endpoints. Routes are relative, without a leading slash. */
interface InventoryApi {

    // ---- Unit products ----

    @POST("api/v1/inventory-unit-products")
    suspend fun createUnitProduct(@Body resource: CreateUnitProductResource): Response<UnitProductResource>

    @GET("api/v1/inventory-unit-products")
    suspend fun getUnitProducts(): Response<List<UnitProductResource>>

    @GET("api/v1/inventory-unit-products/{id}")
    suspend fun getUnitProduct(@Path("id") id: Long): Response<UnitProductResource>

    @PUT("api/v1/inventory-unit-products/{id}")
    suspend fun updateUnitProduct(
        @Path("id") id: Long,
        @Body resource: UpdateUnitProductResource
    ): Response<UnitProductResource>

    // ---- Weight products ----

    @POST("api/v1/inventory-weight-products")
    suspend fun createWeightProduct(@Body resource: CreateWeightProductResource): Response<WeightProductResource>

    @GET("api/v1/inventory-weight-products")
    suspend fun getWeightProducts(): Response<List<WeightProductResource>>

    @GET("api/v1/inventory-weight-products/{id}")
    suspend fun getWeightProduct(@Path("id") id: Long): Response<WeightProductResource>

    @PUT("api/v1/inventory-weight-products/{id}")
    suspend fun updateWeightProduct(
        @Path("id") id: Long,
        @Body resource: UpdateWeightProductResource
    ): Response<WeightProductResource>

    // ---- Unit lots ----

    @POST("api/v1/inventory-unit-lots")
    suspend fun createUnitLot(@Body resource: CreateUnitLotResource): Response<UnitLotResource>

    @GET("api/v1/inventory-unit-lots")
    suspend fun getUnitLots(): Response<List<UnitLotResource>>

    @GET("api/v1/inventory-unit-lots/{id}")
    suspend fun getUnitLot(@Path("id") id: Long): Response<UnitLotResource>

    // ---- Weight lots ----

    @POST("api/v1/inventory-weight-lots")
    suspend fun createWeightLot(@Body resource: CreateWeightLotResource): Response<WeightLotResource>

    @GET("api/v1/inventory-weight-lots")
    suspend fun getWeightLots(): Response<List<WeightLotResource>>

    @GET("api/v1/inventory-weight-lots/{id}")
    suspend fun getWeightLot(@Path("id") id: Long): Response<WeightLotResource>

    // ---- Stock alerts ----

    @GET("api/v1/inventory-stock-alerts")
    suspend fun getStockAlerts(): Response<List<StockAlertResource>>
}
