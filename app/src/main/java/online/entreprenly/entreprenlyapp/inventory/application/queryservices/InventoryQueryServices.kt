package online.entreprenly.entreprenlyapp.inventory.application.queryservices

import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllLotsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllProductsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetLotQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetProductQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetStockAlertsQuery
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ProductQueryService {
    suspend fun handle(query: GetAllProductsQuery): Result<List<Product>>
    suspend fun handle(query: GetProductQuery): Result<Product>
}

interface LotQueryService {
    suspend fun handle(query: GetAllLotsQuery): Result<List<Lot>>
    suspend fun handle(query: GetLotQuery): Result<Lot>
}

interface StockAlertQueryService {
    suspend fun handle(query: GetStockAlertsQuery): Result<List<StockAlert>>
}
