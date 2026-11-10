package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutral
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutralContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.BrandBrown
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccessContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.subscription.domain.model.aggregates.SubscriptionPlan

fun subscriptionMoney(amount: BigDecimal, currency: String): String {
    val symbol = if (currency == "PEN") "S/" else Currency.getInstance(currency).symbol
    val number = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = if (amount.signum() == 0) 0 else 2
        maximumFractionDigits = 2
    }.format(amount)
    return "$symbol $number"
}

@Composable
fun SubscriptionBadge(text: String, warning: Boolean = false, neutral: Boolean = false) {
    Surface(shape = RoundedCornerShape(20.dp), color = if (neutral) StatusNeutralContainer else if (warning) MaterialTheme.extraColors.warningBackground else StatusSuccessContainer) {
        Text(text, Modifier.padding(horizontal = 9.dp, vertical = 3.dp), color = if (neutral) StatusNeutral else if (warning) MaterialTheme.extraColors.warning else StatusSuccess, style = MaterialTheme.typography.labelSmall)
    }
}
@Composable
fun SubscriptionPlanCard(plan: SubscriptionPlan, selected: Boolean, onSelect: (() -> Unit)?) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.extraColors.border)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, fontWeight = FontWeight.Bold)
                SubscriptionBadge(stringResource(if (onSelect == null) R.string.subscription_current_plan else R.string.subscription_recommended), warning = onSelect != null, neutral = onSelect == null)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(subscriptionMoney(plan.amount, plan.currency), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.subscription_per_month), style = MaterialTheme.typography.bodySmall)
            }
            val features = if (onSelect == null) listOf(R.string.subscription_inventory, R.string.subscription_sales) else listOf(R.string.subscription_all_free, R.string.subscription_whatsapp, R.string.subscription_iot, R.string.subscription_reconciliation)
            features.forEach { feature ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp), tint = StatusSuccess)
                    Text(stringResource(feature), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (onSelect != null) {
                OutlinedButton(onClick = onSelect, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) BrandBrown else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) androidx.compose.ui.graphics.Color.White else BrandBrown), border = BorderStroke(1.dp, BrandBrown)) {
                    if (selected) Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Text(stringResource(if (selected) R.string.subscription_selected else R.string.subscription_choose))
                }
            }
        }
    }
}
@Composable
fun SubscriptionField(value: String, onChange: (String) -> Unit, label: String, error: Boolean, modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text, secret: Boolean = false, enabled: Boolean = true, errorMessage: String = stringResource(R.string.subscription_invalid_field)) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
        OutlinedTextField(value = value, onValueChange = onChange, enabled = enabled, singleLine = true, isError = error,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
            supportingText = if (error) ({ Text(errorMessage) }) else null)
    }
}
@Composable
fun SubscriptionSummaryRow(label: String, value: String, total: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(value, Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End,
            style = if (total) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SubscriptionFeaturesCard(onWhatsApp: () -> Unit, onHistory: () -> Unit) {
    AppCard {
        Column(Modifier.padding(horizontal = 14.dp)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onWhatsApp).height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.ChatBubbleOutline, null, Modifier.size(20.dp), tint = StatusSuccess)
                Text(stringResource(R.string.subscription_whatsapp), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                SubscriptionBadge(stringResource(R.string.subscription_included))
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Balance, null, Modifier.size(20.dp), tint = StatusSuccess)
                Text(stringResource(R.string.subscription_iot_device), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                SubscriptionBadge(stringResource(R.string.subscription_included))
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().clickable(onClick = onHistory).height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.ReceiptLong, null, Modifier.size(20.dp), tint = MaterialTheme.extraColors.muted)
                Text(stringResource(R.string.subscription_history), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.extraColors.muted)
            }
        }
    }
}
