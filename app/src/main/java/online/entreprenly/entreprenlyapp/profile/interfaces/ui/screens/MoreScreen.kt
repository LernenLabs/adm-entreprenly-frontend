package online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileHeaderCard
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileUiState
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.NavigationCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton

/** "More" tab: access to the profile, preferences, notifications, subscription and sign out. */
@Composable
fun MoreScreen(
    viewModel: ProfileViewModel,
    onProfile: () -> Unit,
    onPreferences: () -> Unit,
    onNotifications: () -> Unit,
    onSubscription: () -> Unit,
    onAccountSecurity: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val profile: Profile? = (state as? ProfileUiState.Loaded)?.profile

    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.more_title))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            profile?.let { ProfileHeaderCard(it, showBiography = false) }
            NavigationCard(stringResource(R.string.more_profile), stringResource(R.string.more_profile_sub), onClick = onProfile)
            NavigationCard(stringResource(R.string.more_preferences), stringResource(R.string.more_preferences_sub), onClick = onPreferences)
            NavigationCard(stringResource(R.string.more_notifications), stringResource(R.string.more_notifications_sub), onClick = onNotifications)
            NavigationCard(stringResource(R.string.more_subscription), stringResource(R.string.more_subscription_sub), onClick = onSubscription)
            NavigationCard(stringResource(R.string.more_account_security), stringResource(R.string.more_account_security_sub), onClick = onAccountSecurity)
            SecondaryPillButton(stringResource(R.string.more_sign_out), onSignOut)
        }
    }
}
