package online.entreprenly.entreprenlyapp.iam.interfaces.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/** Password rules shown while signing up (the backend itself only requires 8+ characters). */
data class PasswordRules(val minLength: Boolean, val uppercase: Boolean, val digit: Boolean) {
    val allMet: Boolean get() = minLength && uppercase && digit

    companion object {
        fun of(password: String) = PasswordRules(
            minLength = password.length >= 8,
            uppercase = password.any { it.isUpperCase() },
            digit = password.any { it.isDigit() }
        )
    }
}

/** Live checklist: neutral while empty, green when met, red when unmet after typing. */
@Composable
fun PasswordRequirements(password: String, modifier: Modifier = Modifier) {
    val rules = PasswordRules.of(password)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Requirement(stringResource(R.string.password_rule_length), rules.minLength, password.isEmpty())
        Requirement(stringResource(R.string.password_rule_uppercase), rules.uppercase, password.isEmpty())
        Requirement(stringResource(R.string.password_rule_digit), rules.digit, password.isEmpty())
    }
}

@Composable
private fun Requirement(text: String, met: Boolean, untouched: Boolean) {
    val extra = MaterialTheme.extraColors
    val (icon, tint) = when {
        untouched -> Icons.Outlined.Circle to extra.muted
        met -> Icons.Filled.CheckCircle to extra.success
        else -> Icons.Filled.Cancel to MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, color = tint, style = MaterialTheme.typography.bodySmall)
    }
}
