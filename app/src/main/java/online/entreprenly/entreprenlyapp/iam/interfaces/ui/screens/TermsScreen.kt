package online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PlainTopBar

/** The same terms are available before registration and from More. */
@Composable
fun TermsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        PlainTopBar(stringResource(R.string.terms_title), onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.terms_intro), style = MaterialTheme.typography.bodyMedium)
            listOf(
                R.string.terms_account_title to R.string.terms_account_body,
                R.string.terms_use_title to R.string.terms_use_body,
                R.string.terms_operations_title to R.string.terms_operations_body,
                R.string.terms_subscription_title to R.string.terms_subscription_body
            ).forEach { (title, body) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
