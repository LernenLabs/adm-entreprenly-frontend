package online.entreprenly.entreprenlyapp.shared.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.NavigationCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/** US-75: home with shortcuts to the main modules. */
@Composable
fun HomeScreen(
    userName: String,
    onSell: () -> Unit,
    onInventory: () -> Unit,
    onOrders: () -> Unit,
    onSubscription: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.home_title))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (userName.isNotBlank()) {
                Text(
                    stringResource(R.string.home_greeting, userName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                stringResource(R.string.home_shortcuts),
                color = MaterialTheme.extraColors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
            NavigationCard(stringResource(R.string.nav_sell), null, onClick = onSell)
            NavigationCard(stringResource(R.string.nav_inventory), null, onClick = onInventory)
            NavigationCard(stringResource(R.string.nav_orders), null, onClick = onOrders)
            NavigationCard(stringResource(R.string.more_subscription), null, onClick = onSubscription)
            NavigationCard(stringResource(R.string.more_profile), null, onClick = onProfile)
        }
    }
}
