package online.entreprenly.entreprenlyapp.inventory.application.internal.commandservices

import java.time.Instant
import online.entreprenly.entreprenlyapp.inventory.application.commandservices.CreateProductResult
import online.entreprenly.entreprenlyapp.inventory.application.commandservices.LotCommandService
import online.entreprenly.entreprenlyapp.inventory.application.commandservices.ProductCommandService
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.ProductRepository
import online.entreprenly.entreprenlyapp.shared.application.result.Result

class ProductCommandServiceImpl(
    private val productRepository: ProductRepository,
    private val lotRepository: LotRepository,
    private val now: () -> Instant = Instant::now
) : ProductCommandService {

    /**
     * Two-step creation: POST the product, then POST an initial lot when the
     * initial stock is > 0 (entryDate = now, no expiry, no codeQR). A lot
     * failure never deletes the product; it surfaces as [CreateProductResult.initialStockFailed].
     */
    override suspend fun handle(command: CreateProductCommand): Result<CreateProductResult> {
        val created = productRepository.create(command)
        if (created is Result.Failure) return created
        val product = (created as Result.Success).value
        if (command.initialStock <= 0.0) return Result.Success(CreateProductResult(product, false))
        val lotCommand = CreateLotCommand(
            productType = product.type,
            productId = product.id,
            codeQR = null,
            entryDate = now(),
            expiryDate = null,
            quantityUnits = if (product.type == ProductType.UNIT) command.initialStock.toInt() else 0,
            quantityKg = if (product.type == ProductType.WEIGHT) command.initialStock else 0.0
        )
        return when (lotRepository.create(lotCommand)) {
            is Result.Success -> Result.Success(CreateProductResult(product, false))
            is Result.Failure -> Result.Success(CreateProductResult(product, true))
        }
    }

    override suspend fun handle(command: UpdateProductCommand): Result<Product> =
        productRepository.update(command)
}

class LotCommandServiceImpl(
    private val lotRepository: LotRepository
) : LotCommandService {
    override suspend fun handle(command: CreateLotCommand): Result<Lot> =
        lotRepository.create(command)
}
