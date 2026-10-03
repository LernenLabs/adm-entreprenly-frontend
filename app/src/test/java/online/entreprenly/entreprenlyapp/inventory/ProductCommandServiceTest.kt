package online.entreprenly.entreprenlyapp.inventory

import java.time.Instant
import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.inventory.application.internal.commandservices.ProductCommandServiceImpl
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.ProductRepository
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductCommandServiceTest {

    private val now = Instant.parse("2026-10-03T00:00:00Z")

    private fun product(id: Long = 1, type: ProductType = ProductType.UNIT) = Product(
        type = type, id = id, name = "Arroz", description = null, codeQR = null,
        price = 6.0, weightGrams = 0.0, brand = null
    )

    private class FakeProducts(
        var product: Product = Product(ProductType.UNIT, 1, "Arroz", null, null, 6.0, 0.0, null),
        var fail: Boolean = false
    ) : ProductRepository {
        var creates = 0
        override suspend fun findAll(): Result<List<Product>> = Result.Success(listOf(product))
        override suspend fun findById(type: ProductType, id: Long): Result<Product> = Result.Success(product)
        override suspend fun create(command: CreateProductCommand): Result<Product> {
            creates++
            return if (fail) Result.Failure(ApplicationError.Validation("bad")) else Result.Success(product)
        }
        override suspend fun update(command: UpdateProductCommand): Result<Product> = Result.Success(product)
    }

    private class FakeLots(var fail: Boolean = false) : LotRepository {
        val created = mutableListOf<CreateLotCommand>()
        override suspend fun findAll(): Result<List<Lot>> = Result.Success(emptyList())
        override suspend fun findById(type: ProductType, id: Long): Result<Lot> =
            Result.Failure(ApplicationError.NotFound("nope"))
        override suspend fun create(command: CreateLotCommand): Result<Lot> {
            created += command
            if (fail) return Result.Failure(ApplicationError.Validation("bad lot"))
            return Result.Success(
                Lot(command.productType, 7, command.productId, null, command.entryDate, command.expiryDate, command.quantityUnits, command.quantityKg)
            )
        }
    }

    @Test
    fun create_withStock_registersInitialLot() = runBlocking {
        val products = FakeProducts()
        val lots = FakeLots()
        val service = ProductCommandServiceImpl(products, lots) { now }
        val result = service.handle(CreateProductCommand("Arroz", ProductType.UNIT, 6.0, 4.0))
        assertTrue(result is Result.Success)
        val value = (result as Result.Success).value
        assertFalse(value.initialStockFailed)
        assertEquals(1, lots.created.size)
        assertEquals(4, lots.created[0].quantityUnits)
        assertEquals(now, lots.created[0].entryDate)
        assertEquals(null, lots.created[0].expiryDate)
    }

    @Test
    fun create_withZeroStock_skipsLot() = runBlocking {
        val products = FakeProducts()
        val lots = FakeLots()
        val service = ProductCommandServiceImpl(products, lots) { now }
        val result = service.handle(CreateProductCommand("Arroz", ProductType.UNIT, 6.0, 0.0))
        assertTrue(result is Result.Success)
        assertFalse((result as Result.Success).value.initialStockFailed)
        assertTrue(lots.created.isEmpty())
    }

    @Test
    fun create_whenLotFails_keepsProductAndFlagsPartialFailure() = runBlocking {
        val products = FakeProducts()
        val lots = FakeLots(fail = true)
        val service = ProductCommandServiceImpl(products, lots) { now }
        val result = service.handle(CreateProductCommand("Arroz", ProductType.UNIT, 6.0, 2.0))
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).value.initialStockFailed)
        assertEquals(1, products.creates)
    }

    @Test
    fun create_weightProduct_sendsKg() = runBlocking {
        val products = FakeProducts(product = product(2, ProductType.WEIGHT))
        val lots = FakeLots()
        val service = ProductCommandServiceImpl(products, lots) { now }
        service.handle(CreateProductCommand("Azucar", ProductType.WEIGHT, 4.2, 3.2))
        assertEquals(3.2, lots.created[0].quantityKg, 0.0)
        assertEquals(0, lots.created[0].quantityUnits)
    }
}
