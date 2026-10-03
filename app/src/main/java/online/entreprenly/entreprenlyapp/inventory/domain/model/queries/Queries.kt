package online.entreprenly.entreprenlyapp.inventory.domain.model.queries

import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType

data object GetAllProductsQuery

data class GetProductQuery(val type: ProductType, val id: Long)

data object GetAllLotsQuery

data class GetLotQuery(val type: ProductType, val id: Long)

data object GetStockAlertsQuery
