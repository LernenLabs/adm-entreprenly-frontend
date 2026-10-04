package online.entreprenly.entreprenlyapp.inventory.application.internal.queryservices

import online.entreprenly.entreprenlyapp.inventory.application.queryservices.LotQueryService
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.ProductQueryService
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.StockAlertQueryService
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllLotsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllProductsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetLotQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetProductQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetStockAlertsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.ProductRepository
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.StockAlertRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ProductQueryServiceImpl(
    private val productRepository: ProductRepository
) : ProductQueryService {
    override suspend fun handle(query: GetAllProductsQuery): Result<List<Product>> =
        productRepository.findAll()

    override suspend fun handle(query: GetProductQuery): Result<Product> =
        productRepository.findById(query.type, query.id)
}

class LotQueryServiceImpl(
    private val lotRepository: LotRepository
) : LotQueryService {
    override suspend fun handle(query: GetAllLotsQuery): Result<List<Lot>> =
        lotRepository.findAll()

    override suspend fun handle(query: GetLotQuery): Result<Lot> =
        lotRepository.findById(query.type, query.id)
}

class StockAlertQueryServiceImpl(
    private val stockAlertRepository: StockAlertRepository
) : StockAlertQueryService {
    override suspend fun handle(query: GetStockAlertsQuery): Result<List<StockAlert>> =
        stockAlertRepository.findAll()
}
