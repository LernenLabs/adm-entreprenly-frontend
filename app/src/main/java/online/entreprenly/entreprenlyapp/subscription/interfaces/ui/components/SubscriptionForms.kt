package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.BillingDetails
import online.entreprenly.entreprenlyapp.subscription.domain.model.valueobjects.CardDetails

@Composable
fun BillingDetailsForm(billing: BillingDetails, invalidFields: Set<String>, enabled: Boolean, onChange: (BillingDetails) -> Unit) {
    SubscriptionField(billing.businessName, { onChange(billing.copy(businessName = it)) }, stringResource(R.string.subscription_billing_name), "name" in invalidFields, enabled = enabled)
    SubscriptionField(billing.documentNumber, { value -> onChange(billing.copy(documentNumber = value.filter(Char::isDigit).take(11))) }, stringResource(R.string.subscription_document), "document" in invalidFields, keyboardType = KeyboardType.Number, enabled = enabled)
    SubscriptionField(billing.fiscalAddress, { onChange(billing.copy(fiscalAddress = it)) }, stringResource(R.string.subscription_address), "address" in invalidFields, enabled = enabled)
    SubscriptionField(billing.receiptEmail, { onChange(billing.copy(receiptEmail = it)) }, stringResource(R.string.subscription_email), "email" in invalidFields, keyboardType = KeyboardType.Email, enabled = enabled)
}

@Composable
fun CardDetailsForm(card: CardDetails, invalid: Set<String>, enabled: Boolean, onChange: (CardDetails) -> Unit) {
    Text(stringResource(R.string.subscription_simulated_payment), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
    SubscriptionField(card.number, { onChange(card.copy(number = it.filter(Char::isDigit).take(16))) }, stringResource(R.string.subscription_card_number), "number" in invalid, keyboardType = KeyboardType.Number, enabled = enabled, errorMessage = stringResource(R.string.subscription_card_number_error))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SubscriptionField(card.expiry, { input ->
            val digits = input.filter(Char::isDigit).take(4)
            onChange(card.copy(expiry = if (digits.length > 2) digits.take(2) + "/" + digits.drop(2) else digits))
        }, stringResource(R.string.subscription_expiry), "expiry" in invalid, Modifier.weight(1f), KeyboardType.Number, enabled = enabled)
        SubscriptionField(card.cvv, { onChange(card.copy(cvv = it.filter(Char::isDigit).take(4))) }, stringResource(R.string.subscription_cvv), "cvv" in invalid, Modifier.weight(1f), KeyboardType.NumberPassword, secret = true, enabled = enabled)
    }
    SubscriptionField(card.holder, { onChange(card.copy(holder = it)) }, stringResource(R.string.subscription_holder), "holder" in invalid, enabled = enabled)
}
