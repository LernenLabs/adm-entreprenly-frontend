package online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects

import java.time.YearMonth

data class CardDetails(val number: String = "", val expiry: String = "", val cvv: String = "", val holder: String = "") {
    fun invalidFields(now: YearMonth = YearMonth.now()): Set<String> = buildSet {
        val digits = number.filter(Char::isDigit)
        val validNumber = digits.matches(Regex("[0-9]{16}")) && digits.any { it != '0' } && passesLuhn(digits)
        if (!validNumber) add("number")
        val parts = expiry.split("/")
        val month = parts.getOrNull(0)?.toIntOrNull()
        val year = parts.getOrNull(1)?.takeIf { it.length == 2 }?.toIntOrNull()?.plus(2000)
        if (month == null || month !in 1..12 || year == null || YearMonth.of(year, month).isBefore(now)) add("expiry")
        if (!cvv.matches(Regex("[0-9]{3,4}"))) add("cvv")
        if (holder.isBlank()) add("holder")
    }
    private fun passesLuhn(digits: String): Boolean {
        val checksum = digits.reversed().mapIndexed { index, digit ->
            val value = digit.digitToInt() * if (index % 2 == 1) 2 else 1
            if (value > 9) value - 9 else value
        }.sum()
        return checksum % 10 == 0
    }

    fun maskedMethod(id: String): PaymentMethod {
        val digits = number.filter(Char::isDigit)
        return PaymentMethod(id, if (digits.startsWith("4")) "Visa" else if (digits.startsWith("5")) "Mastercard" else "Card", digits.takeLast(4), holder.trim(), expiry.substringBefore("/"), "20" + expiry.substringAfter("/"))
    }
}
