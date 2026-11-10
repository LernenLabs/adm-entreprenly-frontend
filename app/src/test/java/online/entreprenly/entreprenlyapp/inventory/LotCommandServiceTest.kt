package online.entreprenly.entreprenlyapp.inventory

import kotlinx.coroutines.runBlocking
import online.entreprenly.entreprenlyapp.inventory.application.internal.commandservices.LotCommandServiceImpl
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.DeleteLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.domain.repositories.LotRepository
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LotCommandServiceTest {

    private class FakeLots(
        private val deleteResult: Result<Unit> = Result.Success(Unit)
    ) : LotRepository {
        var deleted: DeleteLotCommand? = null

        override suspend fun findAll(): Result<List<Lot>> = Result.Success(emptyList())

        override suspend fun findById(type: ProductType, id: Long): Result<Lot> =
            Result.Failure(ApplicationError.NotFound("not used"))

        override suspend fun create(command: CreateLotCommand): Result<Lot> =
            Result.Failure(ApplicationError.Unexpected("not used"))

        override suspend fun delete(command: DeleteLotCommand): Result<Unit> {
            deleted = command
            return deleteResult
        }
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
        val lots = FakeLots(Result.Failure(error))
        val service = LotCommandServiceImpl(lots)

        val result = service.handle(DeleteLotCommand(ProductType.UNIT, 7))

        assertTrue(result is Result.Failure)
        assertEquals(error, (result as Result.Failure).error)
    }
}
