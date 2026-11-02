package online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects

import java.time.LocalDate
import java.time.ZoneId

/**
 * The store's calendar day. The backend groups sales by Lima time, so asking with the device's own
 * time zone would look at the wrong day for part of the day on phones set to another zone.
 */
object BusinessDay {
    private val ZONE: ZoneId = ZoneId.of("America/Lima")

    fun today(): LocalDate = LocalDate.now(ZONE)
}
