package online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects



data class BillingDetails(val businessName: String = "", val documentNumber: String = "", val fiscalAddress: String = "", val receiptEmail: String = "") {
    fun invalidFields(): Set<String> = buildSet {
        if (businessName.isBlank()) add("name")
        if (!documentNumber.matches(Regex("(?:[0-9]{8}|[0-9]{11})"))) add("document")
        if (fiscalAddress.isBlank()) add("address")
        if (receiptEmail.isNotBlank() && !receiptEmail.matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) add("email")
    }
}
data class PaymentMethod(val id: String, val cardBrand: String, val lastFour: String, val holderName: String, val expiryMonth: String, val expiryYear: String, val isDefault: Boolean = true)
