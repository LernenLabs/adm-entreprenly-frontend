package online.entreprenly.entreprenlyapp.inventory

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertSeverity
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.AlertType
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.LotStatus
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryRulesTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 3)

    private fun instantOf(date: LocalDate): Instant = InventoryRules.localDateToInstant(date)

    private fun unitLot(
        id: Long = 1,
        productId: Long = 10,
        quantity: Int = 5,
        expiry: LocalDate? = null
    ) = Lot(
        type = ProductType.UNIT,
        id = id,
        productId = productId,
        codeQR = null,
        entryDate = instantOf(today),
        expiryDate = expiry?.let(::instantOf),
        quantityUnits = quantity,
        quantityKg = 0.0
    )

    private fun weightLot(id: Long = 1, productId: Long = 20, kg: Double = 2.5) = Lot(
        type = ProductType.WEIGHT,
        id = id,
        productId = productId,
        codeQR = null,
        entryDate = instantOf(today),
        expiryDate = null,
        quantityUnits = 0,
        quantityKg = kg
    )

    private fun unitProduct(id: Long = 10) = Product(
        type = ProductType.UNIT, id = id, name = "Arroz", description = null,
        codeQR = null, price = 5.0, weightGrams = 0.0, brand = null
    )

    private fun weightProduct(id: Long = 20) = Product(
        type = ProductType.WEIGHT, id = id, name = "Azucar", description = null,
        codeQR = null, price = 4.0, weightGrams = 0.0, brand = null
    )

    // ---- Lot status borders ----

    @Test
    fun lotStatus_coversAllBorders() {
        assertEquals(LotStatus.OUT_OF_STOCK, InventoryRules.lotStatus(unitLot(quantity = 0, expiry = today.minusDays(1)), today))
        assertEquals(LotStatus.EXPIRED, InventoryRules.lotStatus(unitLot(expiry = today.minusDays(1)), today))
        assertEquals(LotStatus.EXPIRING_SOON, InventoryRules.lotStatus(unitLot(expiry = today), today))
        assertEquals(LotStatus.EXPIRING_SOON, InventoryRules.lotStatus(unitLot(expiry = today.plusDays(1)), today))
        assertEquals(LotStatus.EXPIRING_SOON, InventoryRules.lotStatus(unitLot(expiry = today.plusDays(5)), today))
        assertEquals(LotStatus.ACTIVE, InventoryRules.lotStatus(unitLot(expiry = today.plusDays(6)), today))
        assertEquals(LotStatus.ACTIVE, InventoryRules.lotStatus(unitLot(expiry = null), today))
    }

    @Test
    fun lotStatus_weightLotsNeverExpire() {
        assertEquals(LotStatus.ACTIVE, InventoryRules.lotStatus(weightLot(kg = 3.0), today))
        assertEquals(LotStatus.OUT_OF_STOCK, InventoryRules.lotStatus(weightLot(kg = 0.0), today))
    }

    // ---- Counters ----

    @Test
    fun lotCounters_activeIncludesExpiringSoon() {
        val lots = listOf(
            unitLot(id = 1, expiry = today.plusDays(10)), // active
            unitLot(id = 2, expiry = today.plusDays(2)), // expiring (also active)
            unitLot(id = 3, expiry = today.minusDays(1)), // expired
            unitLot(id = 4, quantity = 0), // out of stock: counted nowhere
            weightLot(id = 5) // active
        )
        val counters = InventoryRules.lotCounters(lots, today)
        assertEquals(3, counters.active)
        assertEquals(1, counters.expiringSoon)
        assertEquals(1, counters.expired)
    }

    // ---- Ordering ----

    @Test
    fun sortLots_expiredFirstThenExpiringThenActiveThenOutOfStock() {
        val expired = unitLot(id = 1, expiry = today.minusDays(2))
        val soon2 = unitLot(id = 2, expiry = today.plusDays(2))
        val soon0 = unitLot(id = 3, expiry = today)
        val activeFar = unitLot(id = 4, expiry = today.plusDays(30))
        val activeNoExpiry = weightLot(id = 5)
        val empty = unitLot(id = 6, quantity = 0, expiry = today.plusDays(30))
        val sorted = InventoryRules.sortLots(
            listOf(activeNoExpiry, empty, activeFar, soon2, expired, soon0), today
        ).map { it.id }
        assertEquals(listOf(1L, 3L, 2L, 4L, 5L, 6L), sorted)
    }

    // ---- Stock ----

    @Test
    fun productStock_sumsOwnLotsOnly() {
        val product = unitProduct(id = 10)
        val lots = listOf(
            unitLot(id = 1, productId = 10, quantity = 3),
            unitLot(id = 2, productId = 10, quantity = 2),
            unitLot(id = 3, productId = 99, quantity = 100)
        )
        assertEquals(5.0, InventoryRules.productStock(product, lots), 0.0)
        assertTrue(InventoryRules.isOutOfStock(unitProduct(id = 11), lots))
    }

    @Test
    fun productStock_weightRoundedTo3Decimals() {
        val product = weightProduct(id = 20)
        val lots = listOf(
            weightLot(id = 1, productId = 20, kg = 0.1),
            weightLot(id = 2, productId = 20, kg = 0.2)
        )
        assertEquals(0.3, InventoryRules.productStock(product, lots), 0.0)
        assertEquals("32.5", InventoryRules.formatKg(32.5))
        assertEquals("3", InventoryRules.formatKg(3.0))
    }

    @Test
    fun isLowStock_matchesBackendAlert() {
        val product = unitProduct(id = 10)
        val alerts = listOf(
            StockAlert(1, null, 10, ProductType.UNIT, "Arroz", AlertType.LOW_STOCK, AlertSeverity.WARNING, null)
        )
        assertTrue(InventoryRules.isLowStock(alerts, product))
        assertFalse(InventoryRules.isLowStock(emptyList(), product))
        assertFalse(InventoryRules.isLowStock(alerts, unitProduct(id = 11)))
    }

    // ---- Search with tildes ----

    @Test
    fun matchesSearch_isCaseAndAccentInsensitive() {
        val product = unitProduct().copy(name = "Café Molido", brand = "León", codeQR = "L-0246")
        assertTrue(InventoryRules.matchesSearch(product, "cafe"))
        assertTrue(InventoryRules.matchesSearch(product, "CAFE"))
        assertTrue(InventoryRules.matchesSearch(product, "leon"))
        assertTrue(InventoryRules.matchesSearch(product, "l-0246"))
        assertTrue(InventoryRules.matchesSearch(product, ""))
        assertFalse(InventoryRules.matchesSearch(product, "arroz"))
    }

    // ---- UTC conversions in America/Lima ----

    @Test
    fun utcConversion_hasNoOffByOneInLima() {
        val default = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Lima")))
        try {
            val date = LocalDate.of(2026, 6, 3)
            val instant = InventoryRules.localDateToInstant(date)
            assertEquals("2026-06-03T00:00:00Z", instant.toString())
            assertEquals(date, InventoryRules.instantToLocalDate(instant))
            assertEquals("03/06/2026", InventoryRules.formatLotDate(instant))
            assertEquals(0L, InventoryRules.daysLeft(instant, date))
        } finally {
            TimeZone.setDefault(default)
        }
    }

    // ---- Validations ----

    @Test
    fun validations_productForms() {
        assertTrue(InventoryRules.isValidProductName(" Arroz "))
        assertFalse(InventoryRules.isValidProductName("   "))
        assertEquals(6.0, InventoryRules.parsePrice("6,00"))
        assertEquals(6.0, InventoryRules.parsePrice("6.00")!!, 0.0)
        assertEquals(null, InventoryRules.parsePrice("-1"))
        assertEquals(null, InventoryRules.parsePrice(""))
        assertEquals(5, InventoryRules.parseUnitStock("5"))
        assertEquals(null, InventoryRules.parseUnitStock("5.5"))
        assertEquals(null, InventoryRules.parseUnitStock("-1"))
        assertEquals(3.2, InventoryRules.parseWeightStock("3,2")!!, 0.0)
    }

    @Test
    fun validations_lotForms() {
        assertTrue(InventoryRules.isValidLotQuantity(ProductType.UNIT, "4"))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.UNIT, "0"))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.UNIT, "1.5"))
        assertTrue(InventoryRules.isValidLotQuantity(ProductType.WEIGHT, "3,2"))
        assertFalse(InventoryRules.isValidLotQuantity(ProductType.WEIGHT, "0"))
        val entry = today
        assertFalse(InventoryRules.isValidExpiry(null, entry, today))
        assertFalse(InventoryRules.isValidExpiry(today.minusDays(1), entry, today))
        assertFalse(InventoryRules.isValidExpiry(today, today.plusDays(1), today))
        assertTrue(InventoryRules.isValidExpiry(today, today, today))
        assertTrue(InventoryRules.isValidExpiry(today.plusDays(5), today, today))
    }

    @Test
    fun initials_followDesignExamples() {
        assertEquals("LG", InventoryRules.initials("Leche Gloria 400 g"))
        assertEquals("MA", InventoryRules.initials("Manzanas"))
        assertEquals("A", InventoryRules.initials("a"))
        assertEquals("", InventoryRules.initials("   "))
    }

    @Test
    fun lotLabel_prefersCodeQr() {
        assertEquals("X-1", unitLot(id = 7).copy(codeQR = "X-1").label)
        assertEquals("L-0007", unitLot(id = 7).label)
    }

    @Test
    fun productAndLotIdentity_isTypePlusId() {
        assertEquals("unit/1", unitProduct(id = 1).key)
        assertEquals("weight/1", weightProduct(id = 1).key)
        assertEquals("unit/2", unitLot(id = 2).key)
    }
}
