package online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects

import java.text.Normalizer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert

/**
 * Pure client-side business rules, mirroring the backend alert generator.
 * No Android dependencies: fully unit-testable on the JVM.
 *
 * All lot dates are stored as UTC midnight instants. Every calculation and
 * format uses UTC so devices in America/Lima (UTC-5) never see an off-by-one day.
 */
object InventoryRules {
    /** Mirror of the backend expiring-soon window. Single source of truth. */
    const val EXPIRING_SOON_DAYS = 5L

    private val DateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT).withZone(ZoneOffset.UTC)

    fun todayUtc(): LocalDate = LocalDate.now(ZoneOffset.UTC)

    fun localDateToInstant(date: LocalDate): Instant =
        date.atStartOfDay(ZoneOffset.UTC).toInstant()

    fun instantToLocalDate(instant: Instant): LocalDate =
        instant.atZone(ZoneOffset.UTC).toLocalDate()

    fun formatLotDate(instant: Instant): String = DateFormat.format(instant)

    fun daysLeft(expiryDate: Instant, today: LocalDate = todayUtc()): Long =
        ChronoUnit.DAYS.between(today, instantToLocalDate(expiryDate))

    /** Priority: OUT_OF_STOCK > EXPIRED > EXPIRING_SOON > ACTIVE. */
    fun lotStatus(lot: Lot, today: LocalDate = todayUtc()): LotStatus {
        if (lot.isEmpty) return LotStatus.OUT_OF_STOCK
        val expiry = lot.expiryDate ?: return LotStatus.ACTIVE
        val left = daysLeft(expiry, today)
        return when {
            left < 0 -> LotStatus.EXPIRED
            left <= EXPIRING_SOON_DAYS -> LotStatus.EXPIRING_SOON
            else -> LotStatus.ACTIVE
        }
    }

    data class LotCounters(val active: Int, val expiringSoon: Int, val expired: Int)

    /** Active = quantity > 0 and not expired (includes expiring-soon). */
    fun lotCounters(lots: List<Lot>, today: LocalDate = todayUtc()): LotCounters {
        var active = 0
        var expiring = 0
        var expired = 0
        for (lot in lots) {
            when (lotStatus(lot, today)) {
                LotStatus.ACTIVE -> active++
                LotStatus.EXPIRING_SOON -> { active++; expiring++ }
                LotStatus.EXPIRED -> expired++
                LotStatus.OUT_OF_STOCK -> Unit
            }
        }
        return LotCounters(active, expiring, expired)
    }

    /**
     * Dashboard order: EXPIRED -> EXPIRING_SOON (fewest days first) ->
     * ACTIVE (nearest expiry first, no expiry last) -> OUT_OF_STOCK.
     */
    fun sortLots(lots: List<Lot>, today: LocalDate = todayUtc()): List<Lot> {
        val statuses = lots.associateWith { lotStatus(it, today) }
        return lots.sortedWith(
            compareBy<Lot> { statusRank(statuses.getValue(it)) }
                .thenBy { daysSortKey(it, statuses.getValue(it), today) }
                .thenBy { it.id }
        )
    }

    private fun statusRank(status: LotStatus): Int = when (status) {
        LotStatus.EXPIRED -> 0
        LotStatus.EXPIRING_SOON -> 1
        LotStatus.ACTIVE -> 2
        LotStatus.OUT_OF_STOCK -> 3
    }

    private fun daysSortKey(lot: Lot, status: LotStatus, today: LocalDate): Long {
        val expiry = lot.expiryDate
        if (expiry == null) return if (status == LotStatus.ACTIVE) Long.MAX_VALUE else Long.MAX_VALUE - 1
        return daysLeft(expiry, today)
    }

    /** Lots belonging to a product: matched by (type, productId). */
    fun lotsOfProduct(product: Product, lots: List<Lot>): List<Lot> =
        lots.filter { it.type == product.type && it.productId == product.id }

    /**
     * Derived stock: sum of lot quantities. Weight is rounded to 3 decimals
     * to avoid floating-point noise.
     */
    fun productStock(product: Product, lots: List<Lot>): Double {
        val own = lotsOfProduct(product, lots)
        return if (product.type == ProductType.UNIT) {
            own.sumOf { it.quantityUnits }.toDouble()
        } else {
            round3(own.sumOf { it.quantityKg })
        }
    }

    /** A product is out of stock when its total stock is <= 0 (includes no lots). */
    fun isOutOfStock(product: Product, lots: List<Lot>): Boolean =
        productStock(product, lots) <= 0.0

    /** Low stock comes from a backend low_stock alert for (productType, productId). */
    fun isLowStock(alerts: List<StockAlert>, product: Product): Boolean =
        alerts.any {
            it.alertType == AlertType.LOW_STOCK &&
                it.productId == product.id &&
                (it.productType == null || it.productType == product.type)
        }

    // ---- Search ----

    fun normalizeSearch(raw: String): String =
        Normalizer.normalize(raw.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

    /** Accent- and case-insensitive "contains" over name, brand and codeQR. */
    fun matchesSearch(product: Product, query: String): Boolean {
        val q = normalizeSearch(query.trim())
        if (q.isEmpty()) return true
        val haystacks = listOfNotNull(product.name, product.brand, product.codeQR)
            .map(::normalizeSearch)
        return haystacks.any { it.contains(q) }
    }

    // ---- Text helpers ----

    /** Two words -> first letter of each; one word -> first two letters; uppercase. */
    fun initials(name: String): String {
        val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (words.isEmpty()) return ""
        return if (words.size == 1) {
            words[0].take(2).uppercase(Locale.ROOT)
        } else {
            "${words[0].first()}${words[1].first()}".uppercase(Locale.ROOT)
        }
    }

    /** Weight amounts without trailing zeros: 32.500 -> "32.5". */
    fun formatKg(amount: Double): String {
        val rounded = round3(amount)
        val asLong = rounded.toLong()
        if (rounded == asLong.toDouble()) return asLong.toString()
        return String.format(Locale.ROOT, "%.3f", rounded).trimEnd('0')
    }

    fun round3(value: Double): Double = (value * 1000.0).roundToInt() / 1000.0

    // ---- Parsing / validation (UI maps failures to strings) ----

    /** Accepts comma or point decimals. */
    fun parseDecimal(raw: String): Double? {
        val normalized = raw.trim().replace(',', '.')
        if (normalized.isEmpty()) return null
        return normalized.toDoubleOrNull()
    }

    fun parsePrice(raw: String): Double? {
        val value = parseDecimal(raw) ?: return null
        return if (value >= 0.0) value else null
    }

    /** Unit stock: non-negative integer. */
    fun parseUnitStock(raw: String): Int? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        if (!trimmed.matches(Regex("[0-9]+"))) return null
        return trimmed.toIntOrNull()
    }

    /** Weight stock: non-negative decimal. */
    fun parseWeightStock(raw: String): Double? {
        val value = parseDecimal(raw) ?: return null
        return if (value >= 0.0) value else null
    }

    fun isValidProductName(name: String): Boolean = name.trim().isNotEmpty()

    /** Lot quantity must be strictly > 0. */
    fun isValidLotQuantity(type: ProductType, quantityText: String): Boolean =
        if (type == ProductType.UNIT) {
            val v = parseUnitStock(quantityText)
            v != null && v > 0
        } else {
            val v = parseWeightStock(quantityText)
            v != null && v > 0.0
        }

    /** Expiry (unit only): required, not before today and not before entry date. */
    fun isValidExpiry(expiry: LocalDate?, entry: LocalDate, today: LocalDate = todayUtc()): Boolean {
        if (expiry == null) return false
        return !expiry.isBefore(today) && !expiry.isBefore(entry)
    }
}
