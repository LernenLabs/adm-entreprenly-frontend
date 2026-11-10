package online.entreprenly.entreprenlyapp.inventory

import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.inventory.application.internal.commandservices.LotCommandServiceImpl
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.DeleteLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * US-09 evidence: valid creation reaches the repository and can be queried
 * back; incomplete or inconsistent data is rejected before the command.
 */
class LotCreationValidationTest {

    private val today = LocalDate.of(2026, 10, 3)
    private val entry = today
    private val expiry = today.plusDays(30)
    private val product = Product(
        type = ProductType.UNIT,
        id = 10,
        name = "Leche Gloria 400 g",
        description = null,
        codeQR = null,
        price = 4.5,
        weightGrams = 400.0,
        brand = "Gloria"
    )

    private class InMemoryLots : LotRepository {
        val stored = linkedMapOf<Pair<ProductType, Long>, Lot>()
        private var nextId = 1L

        override suspend fun findAll(): Result<List<Lot>> = Result.Success(stored.values.toList())

        override suspend fun findById(type: ProductType, id: Long): Result<Lot> =
            stored[type to id]?.let { Result.Success(it) }
                ?: Result.Failure(ApplicationError.NotFound("Lot not found"))

        override suspend fun create(command: CreateLotCommand): Result<Lot> {
            val lot = Lot(
                type = command.productType,
                id = nextId++,
                productId = command.productId,
                codeQR = command.codeQR,
                entryDate = command.entryDate,
                expiryDate = command.expiryDate,
                quantityUnits = command.quantityUnits,
                quantityKg = command.quantityKg
            )
            stored[lot.type to lot.id] = lot
            return Result.Success(lot)
        }

        override suspend fun update(command: UpdateLotCommand): Result<Lot> =
            Result.Failure(ApplicationError.Unexpected("not used"))

        override suspend fun delete(command: DeleteLotCommand): Result<Unit> =
            Result.Failure(ApplicationError.Unexpected("not used"))
    }

    @Test
    fun validCreation_persistsLotAndRequeryReturnsIt() = runBlocking {
        assertTrue(InventoryRules.isValidLotQuantity(product.type, "12"))
        assertTrue(InventoryRules.isValidExpiry(expiry, entry, today))

        val command = CreateLotCommand(
            productType = product.type,
            productId = product.id,
            codeQR = "L-2901",
            entryDate = InventoryRules.localDateToInstant(entry),
            expiryDate = InventoryRules.localDateToInstant(expiry),
            quantityUnits = 12,
            quantityKg = 0.0
        )
        val lots = InMemoryLots()
        val service = LotCommandServiceImpl(lots)

        val created = service.handle(command)
        assertTrue(created is Result.Success)
        val createdId = (created as Result.Success).value.id

        val requery = lots.findById(product.type, createdId)
        assertTrue(requery is Result.Success)
        val lot = (requery as Result.Success).value
        assertEquals(product.id, lot.productId)
        assertEquals("L-2901", lot.codeQR)
        assertEquals(12, lot.quantityUnits)
        assertEquals(InventoryRules.localDateToInstant(entry), lot.entryDate)
        assertEquals(InventoryRules.localDateToInstant(expiry), lot.expiryDate)
        assertEquals(12.0, InventoryRules.productStock(product, lots.stored.values.toList()), 0.0)
    }

    @Test
    fun incompleteData_isRejectedBeforeCreatingCommand() {
        // Without a selected product the sheet cannot build a command.
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.UNIT, ""))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.UNIT, "0"))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.UNIT, "doce"))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.WEIGHT, ""))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.WEIGHT, "0"))

        // Unit lots require an expiry that is not before entry or today.
        assertFalse(InventoryRules.isValidExpiry(null, entry, today))
        assertFalse(InventoryRules.isValidExpiry(entry.minusDays(1), entry, today))
        assertFalse(InventoryRules.isValidExpiry(entry, entry.plusDays(1), today))
    }

    @Test
    fun weightCreation_persistsKgWithoutExpiry() = runBlocking {
        val weightProduct = product.copy(type = ProductType.WEIGHT, id = 20, name = "Azúcar")
        assertTrue(InventoryRules.isValidLotQuantity(weightProduct.type, "2,5"))

        val command = CreateLotCommand(
            productType = weightProduct.type,
            productId = weightProduct.id,
            codeQR = null,
            entryDate = InventoryRules.localDateToInstant(entry),
            expiryDate = null,
            quantityUnits = 0,
            quantityKg = 2.5
        )
        val lots = InMemoryLots()
        val service = LotCommandServiceImpl(lots)

        val created = service.handle(command)
        assertTrue(created is Result.Success)
        val createdLot = (created as Result.Success).value
        val lot = lots.stored[createdLot.type to createdLot.id]!!
        assertEquals(2.5, lot.quantityKg, 0.0)
        assertEquals(null, lot.expiryDate)
        assertEquals(2.5, InventoryRules.productStock(weightProduct, lots.stored.values.toList()), 0.0)
    }
}
