package online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.ProductRepository
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.StockAlertRepository
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.api.InventoryApi
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toEntity
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toUnitResource
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toWeightResource
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCall
import online.entreprenly.entreprenlyapp.shared.infrastructure.remote.configuration.safeApiCallWithBody

class ProductRepositoryImpl(private val api: InventoryApi) : ProductRepository {

    override suspend fun findAll(): Result<List<Product>> = coroutineScope {
        val units = async { safeApiCall({ api.getUnitProducts() }) { it.orEmpty().map { r -> r.toEntity() } } }
        val weights = async { safeApiCall({ api.getWeightProducts() }) { it.orEmpty().map { r -> r.toEntity() } } }
        when (val u = units.await()) {
            is Result.Failure -> u
            is Result.Success -> when (val w = weights.await()) {
                is Result.Failure -> w
                is Result.Success -> Result.Success(u.value + w.value)
            }
        }
    }

    override suspend fun findById(type: ProductType, id: Long): Result<Product> =
        when (type) {
            ProductType.UNIT -> safeApiCallWithBody({ api.getUnitProduct(id) }) { it.toEntity() }
            ProductType.WEIGHT -> safeApiCallWithBody({ api.getWeightProduct(id) }) { it.toEntity() }
        }

    override suspend fun create(command: CreateProductCommand): Result<Product> =
        when (command.type) {
            ProductType.UNIT ->
                safeApiCallWithBody({ api.createUnitProduct(command.toUnitResource()) }) { it.toEntity() }
            ProductType.WEIGHT ->
                safeApiCallWithBody({ api.createWeightProduct(command.toWeightResource()) }) { it.toEntity() }
        }

    override suspend fun update(command: UpdateProductCommand): Result<Product> =
        when (command.type) {
            ProductType.UNIT ->
                safeApiCallWithBody({ api.updateUnitProduct(command.id, command.toUnitResource()) }) { it.toEntity() }
            ProductType.WEIGHT ->
                safeApiCallWithBody({ api.updateWeightProduct(command.id, command.toWeightResource()) }) { it.toEntity() }
        }
}

class LotRepositoryImpl(private val api: InventoryApi) : LotRepository {

    override suspend fun findAll(): Result<List<Lot>> = coroutineScope {
        val units = async { safeApiCall({ api.getUnitLots() }) { it.orEmpty().map { r -> r.toEntity() } } }
        val weights = async { safeApiCall({ api.getWeightLots() }) { it.orEmpty().map { r -> r.toEntity() } } }
        when (val u = units.await()) {
            is Result.Failure -> u
            is Result.Success -> when (val w = weights.await()) {
                is Result.Failure -> w
                is Result.Success -> Result.Success(u.value + w.value)
            }
        }
    }

    override suspend fun findById(type: ProductType, id: Long): Result<Lot> =
        when (type) {
            ProductType.UNIT -> safeApiCallWithBody({ api.getUnitLot(id) }) { it.toEntity() }
            ProductType.WEIGHT -> safeApiCallWithBody({ api.getWeightLot(id) }) { it.toEntity() }
        }

    override suspend fun create(command: CreateLotCommand): Result<Lot> =
        when (command.productType) {
            ProductType.UNIT ->
                safeApiCallWithBody({ api.createUnitLot(command.toUnitResource()) }) { it.toEntity() }
            ProductType.WEIGHT ->
                safeApiCallWithBody({ api.createWeightLot(command.toWeightResource()) }) { it.toEntity() }
        }
}

class StockAlertRepositoryImpl(private val api: InventoryApi) : StockAlertRepository {
    override suspend fun findAll(): Result<List<StockAlert>> =
        safeApiCall({ api.getStockAlerts() }) { body ->
            body.orEmpty().map { it.toEntity() }
        }

    /**
     * Degraded fallback: callers treat alerts as optional, so a failure maps
     * to an empty list instead of an error state.
     */
    suspend fun findAllOrEmpty(): List<StockAlert> =
        when (val r = findAll()) {
            is Result.Success -> r.value
            is Result.Failure -> {
                if (r.error is ApplicationError) emptyList() else emptyList()
            }
        }
}
