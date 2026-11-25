package online.entreprenly.entreprenlyapp.inventory.domain.repositories

import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.DeleteLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.DeleteProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UploadProductImageCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.BarcodeProduct
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.shared.application.result.Result

/** Stores product photos and returns the imageUrl to save in the product. */
interface ProductImageRepository {
    suspend fun upload(command: UploadProductImageCommand): Result<String>
}

/** Finds suggested data for a package barcode; NotFound when the barcode is unknown. */
interface BarcodeLookupRepository {
    suspend fun lookup(barcode: String): Result<BarcodeProduct>
}

interface ProductRepository {
    suspend fun findAll(): Result<List<Product>>
    suspend fun findById(type: ProductType, id: Long): Result<Product>
    suspend fun create(command: CreateProductCommand): Result<Product>
    suspend fun update(command: UpdateProductCommand): Result<Product>
    suspend fun delete(command: DeleteProductCommand): Result<Unit>
}

interface LotRepository {
    suspend fun findAll(): Result<List<Lot>>
    suspend fun findById(type: ProductType, id: Long): Result<Lot>
    suspend fun create(command: CreateLotCommand): Result<Lot>
    suspend fun update(command: UpdateLotCommand): Result<Lot>
    suspend fun delete(command: DeleteLotCommand): Result<Unit>
}

interface StockAlertRepository {
    suspend fun findAll(): Result<List<StockAlert>>
}
