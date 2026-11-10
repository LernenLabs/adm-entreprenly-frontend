package online.entreprenly.entreprenlyapp.inventory

import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.inventory.application.internal.commandservices.LotCommandServiceImpl
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.DeleteLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LotCommandServiceTest {

    private class FakeLots(
        private val updateResult: Result<Lot> = Result.Failure(ApplicationError.Unexpected("not used")),
        private val deleteResult: Result<Unit> = Result.Success(Unit)
    ) : LotRepository {
        var updated: UpdateLotCommand? = null
        var deleted: DeleteLotCommand? = null

        override suspend fun findAll(): Result<List<Lot>> = Result.Success(emptyList())

        override suspend fun findById(type: ProductType, id: Long): Result<Lot> =
            Result.Failure(ApplicationError.NotFound("not used"))

        override suspend fun create(command: CreateLotCommand): Result<Lot> =
            Result.Failure(ApplicationError.Unexpected("not used"))

        override suspend fun update(command: UpdateLotCommand): Result<Lot> {
            updated = command
            return updateResult
        }

        override suspend fun delete(command: DeleteLotCommand): Result<Unit> {
            deleted = command
            return deleteResult
        }
    }

    @Test
    fun update_forwardsFullCommand() = runBlocking {
        val command = UpdateLotCommand(
            type = ProductType.UNIT,
            id = 8,
            productId = 3,
            codeQR = "QR-8",
            entryDate = java.time.Instant.parse("2026-10-01T00:00:00Z"),
            expiryDate = java.time.Instant.parse("2026-12-01T00:00:00Z"),
            quantityUnits = 12,
            quantityKg = 0.0
        )
        val lots = FakeLots(updateResult = Result.Success(
            Lot(ProductType.UNIT, 8, 3, "QR-8", command.entryDate, command.expiryDate, 12, 0.0)
        ))
        val service = LotCommandServiceImpl(lots)

        val result = service.handle(command)

        assertTrue(result is Result.Success)
        assertEquals(command, lots.updated)
    }

    @Test
    fun update_propagatesApiFailure() = runBlocking {
        val error = ApplicationError.NotFound("Lot not found")
        val lots = FakeLots(updateResult = Result.Failure(error))
        val service = LotCommandServiceImpl(lots)

        val result = service.handle(
            UpdateLotCommand(
                ProductType.WEIGHT, 9, 4, null,
                java.time.Instant.parse("2026-10-01T00:00:00Z"), null, 0, 2.5
            )
        )

        assertTrue(result is Result.Failure)
        assertEquals(error, (result as Result.Failure).error)
    }

    @Test
    fun delete_forwardsTypeAndId() = runBlocking {
        val lots = FakeLots()
        val service = LotCommandServiceImpl(lots)

        val result = service.handle(DeleteLotCommand(ProductType.WEIGHT, 42))

        assertTrue(result is Result.Success)
        assertEquals(DeleteLotCommand(ProductType.WEIGHT, 42), lots.deleted)
    }

    @Test
    fun delete_propagatesApiFailure() = runBlocking {
        val error = ApplicationError.NotFound("Lot not found")
        val lots = FakeLots(deleteResult = Result.Failure(error))
        val service = LotCommandServiceImpl(lots)

        val result = service.handle(DeleteLotCommand(ProductType.UNIT, 7))

        assertTrue(result is Result.Failure)
        assertEquals(error, (result as Result.Failure).error)
    }
}
