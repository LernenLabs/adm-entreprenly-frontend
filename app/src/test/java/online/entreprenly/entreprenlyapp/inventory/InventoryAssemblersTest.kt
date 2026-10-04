package online.entreprenly.entreprenlyapp.inventory

import java.time.Instant
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toEntity
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toResourceString
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toUnitResource
import online.entreprenly.entreprenlyapp.inventory.infrastructure.remote.assemblers.toWeightResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.StockAlertResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UnitLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.UnitProductResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.WeightLotResource
import online.entreprenly.entreprenlyapp.inventory.interfaces.rest.resources.WeightProductResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InventoryAssemblersTest {

    @Test
    fun unitProductResource_mapsToDomain() {
        val entity = UnitProductResource(1, "Arroz", "desc", "QR1", "unit", 6.0, 500.0, "Marca").toEntity()
        assertEquals(ProductType.UNIT, entity.type)
        assertEquals(1L, entity.id)
        assertEquals(6.0, entity.price, 0.0)
        assertEquals(500.0, entity.weightGrams, 0.0)
        assertEquals("Marca", entity.brand)
    }

    @Test
    fun weightProductResource_mapsToDomain() {
        val entity = WeightProductResource(2, "Azucar", null, null, "weight", 4.2).toEntity()
        assertEquals(ProductType.WEIGHT, entity.type)
        assertEquals(4.2, entity.price, 0.0)
    }

    @Test
    fun lotResources_mapDatesAndQuantities() {
        val unit = UnitLotResource(1, 10, null, "2026-10-03T00:00:00Z", "unit", 4, "2026-10-08T00:00:00Z").toEntity()
        assertEquals(4, unit.quantityUnits)
        assertEquals(Instant.parse("2026-10-03T00:00:00Z"), unit.entryDate)
        assertEquals(Instant.parse("2026-10-08T00:00:00Z"), unit.expiryDate)

        val weight = WeightLotResource(2, 20, "QR", "2026-10-03T00:00:00Z", "weight", 3.2).toEntity()
        assertEquals(3.2, weight.quantityKg, 0.0)
        assertNull(weight.expiryDate)
    }

    @Test
    fun invalidDateStrings_degradeToEpochOrNull() {
        val unit = UnitLotResource(1, 10, null, "not-a-date", "unit", 1, "also-bad").toEntity()
        assertEquals(Instant.EPOCH, unit.entryDate)
        assertNull(unit.expiryDate)
    }

    @Test
    fun alertResource_withUnknownType_mapsToNull() {
        val alert = StockAlertResource(9, null, 10, "unit", "Arroz", "expired", "critical", "msg", "bad-date").toEntity()
        assertEquals(10L, alert.productId)
        assertNull(alert.createdAt)
    }

    @Test
    fun createCommands_serializeDatesAsUtcMidnight() {
        val entry = Instant.parse("2026-10-03T00:00:00Z")
        val expiry = Instant.parse("2026-10-08T00:00:00Z")
        val lot = CreateLotCommand(ProductType.UNIT, 10, " ", entry, expiry, 4, 0.0).toUnitResource()
        assertEquals(10L, lot.productId)
        assertEquals(null, lot.codeQR)
        assertEquals("2026-10-03T00:00:00Z", lot.entryDate)
        assertEquals("2026-10-08T00:00:00Z", lot.expiryDate)

        val product = CreateProductCommand(" Arroz ", ProductType.UNIT, 6.0, 2.0).toUnitResource()
        assertEquals("Arroz", product.name)

        val weightLot = CreateLotCommand(ProductType.WEIGHT, 20, null, entry, null, 0, 3.2).toWeightResource()
        assertEquals(3.2, weightLot.quantityKg, 0.0)
    }

    @Test
    fun instant_formatsAsIsoUtc() {
        assertEquals("2026-10-03T00:00:00Z", Instant.parse("2026-10-03T00:00:00Z").toResourceString())
    }
}
