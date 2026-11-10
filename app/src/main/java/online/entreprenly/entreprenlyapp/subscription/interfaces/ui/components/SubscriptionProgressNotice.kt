package online.entreprenly.entreprenlyapp.subscription.interfaces.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

@Composable
fun SubscriptionProgressNotice(title: String, message: String) {
    val colors = MaterialTheme.extraColors
    Surface(shape = RoundedCornerShape(12.dp), color = colors.warning) {
        Row(
            Modifier.fillMaxWidth().padding(start = 3.dp)
                .background(colors.warningBackground).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(Modifier.size(18.dp), color = colors.warning, strokeWidth = 2.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.warning)
                Text(message, style = MaterialTheme.typography.bodySmall, color = colors.warning)
            }
        }
    }
}
