package online.entreprenly.entreprenlyapp.profile.interfaces.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

@Composable
fun ContactVerificationLabel(verified: Boolean) {
    val colors = MaterialTheme.extraColors
    val foreground = if (verified) colors.success else colors.warning
    val background = if (verified) colors.successBackground else colors.warningBackground
    Surface(modifier = Modifier.padding(top = 6.dp), shape = CircleShape, color = background) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (verified) Icon(Icons.Filled.Check, null, Modifier.size(14.dp), tint = foreground)
            Text(
                text = stringResource(if (verified) R.string.profile_contact_verified else R.string.profile_contact_pending),
                color = foreground,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
