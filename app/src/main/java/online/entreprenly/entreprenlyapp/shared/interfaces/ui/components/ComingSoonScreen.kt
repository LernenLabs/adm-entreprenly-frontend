package online.entreprenly.entreprenlyapp.shared.interfaces.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/**
 * Placeholder for a module that is not implemented yet (US-83). Replace the call in the module's
 * navigation graph with the real screen.
 */
@Composable
fun ComingSoonScreen(title: String, onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        AppTopBar(title, onBack)
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.module_coming_soon_title, title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.module_coming_soon_message),
                color = MaterialTheme.extraColors.muted,
                textAlign = TextAlign.Center
            )
        }
    }
}
