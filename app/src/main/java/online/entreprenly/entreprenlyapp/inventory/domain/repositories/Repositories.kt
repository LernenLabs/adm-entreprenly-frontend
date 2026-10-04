package online.entreprenly.entreprenlyapp.inventory.domain.repositories

import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.shared.application.result.Result

interface ProductRepository {
    suspend fun findAll(): Result<List<Product>>
    suspend fun findById(type: ProductType, id: Long): Result<Product>
    suspend fun create(command: CreateProductCommand): Result<Product>
    suspend fun update(command: UpdateProductCommand): Result<Product>
}

interface LotRepository {
    suspend fun findAll(): Result<List<Lot>>
    suspend fun findById(type: ProductType, id: Long): Result<Lot>
    suspend fun create(command: CreateLotCommand): Result<Lot>
}

interface StockAlertRepository {
    suspend fun findAll(): Result<List<StockAlert>>
}
