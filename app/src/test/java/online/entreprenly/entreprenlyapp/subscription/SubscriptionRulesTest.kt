package online.entreprenly.entreprenlyapp.subscription

import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.SubscriptionStatus
import java.time.Instant
import java.time.YearMonth
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.Subscription
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.CardDetails
import org.junit.Assert.*
import org.junit.Test

class SubscriptionRulesTest {
    private val now = Instant.parse("2026-10-05T00:00:00Z")
    private val billing = BillingDetails("Bodega El Huerto", "10456789123", "Jr. Huánuco 412", "lucho@example.com")
    private val card = CardDetails("4111111111111111", "08/29", "123", "LUIS QUISPE")

    @Test fun validDniAndRucAreAccepted() {
        assertTrue(billing.invalidFields().isEmpty())
        assertTrue(billing.copy(documentNumber = "12345678").invalidFields().isEmpty())
    }
    @Test fun billingErrorsIdentifyTheAffectedFields() {
        assertEquals(setOf("name", "document", "address", "email"), BillingDetails(receiptEmail = "invalid").invalidFields())
        assertEquals(setOf("document"), billing.copy(documentNumber = "123456789").invalidFields())
    }
    @Test fun receiptEmailIsOptionalButMustBeValidWhenProvided() {
        assertTrue(billing.copy(receiptEmail = "").invalidFields().isEmpty())
        assertEquals(setOf("email"), billing.copy(receiptEmail = "a b@example.com").invalidFields())
    }
    @Test fun onlyActiveUnexpiredSubscriptionsGrantPaidAccess() {
        listOf("PENDING_PAYMENT", "CANCELLED", "EXPIRED", "SUSPENDED").forEach { status ->
            assertFalse(Subscription(1, 2, SubscriptionStatus.valueOf(status), now.plusSeconds(60), null).isActiveAt(now))
        }
        assertTrue(Subscription(1, 2, SubscriptionStatus.ACTIVE, now.plusSeconds(60), 1).isActiveAt(now))
        assertFalse(Subscription(1, 2, SubscriptionStatus.ACTIVE, now, 1).isActiveAt(now))
        assertFalse(Subscription(1, 2, SubscriptionStatus.ACTIVE, null, 1).isActiveAt(now))
    }
    @Test fun cardRequiresSixteenDigitsLuhnAndValidExpiry() {
        assertTrue(card.invalidFields(YearMonth.of(2026, 10)).isEmpty())
        assertEquals(setOf("number"), card.copy(number = "4111111111111112").invalidFields(YearMonth.of(2026, 10)))
        assertEquals(setOf("number"), card.copy(number = "0000000000000000").invalidFields(YearMonth.of(2026, 10)))
        assertEquals(setOf("number"), card.copy(number = "411111111111111").invalidFields(YearMonth.of(2026, 10)))
        assertEquals(setOf("number"), card.copy(number = "41111111111111111").invalidFields(YearMonth.of(2026, 10)))
        assertEquals(setOf("expiry"), card.copy(expiry = "09/26").invalidFields(YearMonth.of(2026, 10)))
        assertEquals(setOf("expiry"), card.copy(expiry = "13/29").invalidFields(YearMonth.of(2026, 10)))
        assertTrue(card.copy(expiry = "10/26").invalidFields(YearMonth.of(2026, 10)).isEmpty())
    }
    @Test fun savedMethodContainsOnlyMaskedCardData() {
        val method = card.maskedMethod("fake-method-test")
        assertEquals("1111", method.lastFour)
        assertEquals("Visa", method.cardBrand)
        assertEquals("2029", method.expiryYear)
        assertFalse(method.toString().contains(card.number))
        assertFalse(method.toString().contains(card.cvv))
    }
}
