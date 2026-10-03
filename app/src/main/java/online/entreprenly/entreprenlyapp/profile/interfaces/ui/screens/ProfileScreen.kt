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
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTheme
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileHeaderCard
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileStateGate
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.BannerKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.MessageBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.NavigationCard

/** US-62: view the current profile (personal data, plan and current preferences). */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    email: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPreferences: () -> Unit,
    onNotifications: () -> Unit,
    onChangeEmail: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val updated by viewModel.profileUpdated.collectAsState()

    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.profile_title), onBack)
        ProfileStateGate(state, onRetry = viewModel::load, onSignIn = onSignIn) { profile ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileHeaderCard(profile)
                if (updated) {
                    MessageBanner(
                        stringResource(R.string.profile_updated_title),
                        stringResource(R.string.profile_updated_message),
                        BannerKind.SUCCESS
                    )
                }
                NavigationCard(stringResource(R.string.profile_email_title), email, onClick = onChangeEmail)
                NavigationCard(
                    stringResource(R.string.profile_phone_title),
                    profile.phone?.takeIf { it.isNotBlank() } ?: stringResource(R.string.profile_phone_empty)
                )
                NavigationCard(
                    stringResource(R.string.profile_edit_title),
                    stringResource(R.string.profile_edit_sub),
                    onClick = {
                        viewModel.dismissProfileUpdated()
                        onEdit()
                    }
                )
                NavigationCard(
                    stringResource(R.string.profile_preferences_title),
                    preferencesSummary(profile),
                    onClick = onPreferences
                )
                NavigationCard(
                    stringResource(R.string.profile_notifications_title),
                    stringResource(R.string.profile_notifications_sub),
                    onClick = onNotifications
                )
            }
        }
    }
}

@Composable
private fun preferencesSummary(profile: Profile): String {
    val p = profile.preferences
    val theme = stringResource(if (p.theme == AppTheme.LIGHT) R.string.prefs_theme_light else R.string.prefs_theme_dark)
    return "${p.language.nativeName} · $theme · ${p.currency.code} (${p.currency.symbol})\n" +
        "${p.timezone.id} (${p.timezone.offset})"
}
