package online.entreprenly.entreprenlyapp.inventory.application.commandservices

import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.shared.application.result.Result

/**
 * Result of the two-step product creation: the product plus whether its
 * initial-stock lot could not be registered. The product is never deleted
 * when the lot fails; the UI shows the partial-failure message instead.
 */
data class CreateProductResult(
    val product: Product,
    val initialStockFailed: Boolean
)

interface ProductCommandService {
    suspend fun handle(command: CreateProductCommand): Result<CreateProductResult>
    suspend fun handle(command: UpdateProductCommand): Result<Product>
}

interface LotCommandService {
    suspend fun handle(command: CreateLotCommand): Result<Lot>
}
