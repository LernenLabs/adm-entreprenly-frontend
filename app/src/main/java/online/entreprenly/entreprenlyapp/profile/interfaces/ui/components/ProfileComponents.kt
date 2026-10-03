package online.entreprenly.entreprenlyapp.profile.interfaces.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileUiState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/** Circle with the user's initials (photo upload is US-64, not part of this delivery). */
@Composable
fun InitialsAvatar(initials: String, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).background(MaterialTheme.extraColors.highlight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** "Merchant · Plan Free" line shown under the user's name. */
@Composable
fun roleAndPlan(profile: Profile): String {
    val role = profile.role?.takeIf { it.isNotBlank() } ?: stringResource(R.string.profile_role_merchant)
    val plan = profile.plan?.takeIf { it.isNotBlank() } ?: return role
    return stringResource(R.string.profile_role_plan, role, plan)
}

/** Header card with avatar, name, role/plan and optional biography. */
@Composable
fun ProfileHeaderCard(profile: Profile, modifier: Modifier = Modifier, showBiography: Boolean = true) {
    AppCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                InitialsAvatar(profile.initials, 76.dp)
                Column {
                    Text(
                        profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        roleAndPlan(profile),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.extraColors.muted
                    )
                }
            }
            if (showBiography && !profile.biography.isNullOrBlank()) {
                Text(
                    profile.biography,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

/**
 * Renders loading, error and expired-session states and calls [content] once the profile is loaded.
 * [onSignIn] is invoked from the expired-session screen (US-62).
 */
@Composable
fun ProfileStateGate(
    state: ProfileUiState,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Profile) -> Unit
) {
    when (state) {
        is ProfileUiState.Loaded -> content(state.profile)
        ProfileUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is ProfileUiState.Error -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(state.message.asString(), textAlign = TextAlign.Center)
            SecondaryPillButton(stringResource(R.string.profile_retry), onRetry)
        }
        ProfileUiState.SessionExpired -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(56.dp))
            Text(
                stringResource(R.string.profile_session_expired_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                stringResource(R.string.profile_session_expired_message),
                color = MaterialTheme.extraColors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Box(Modifier.weight(1f, fill = false))
            PrimaryPillButton(stringResource(R.string.profile_sign_in), onSignIn)
        }
    }
}
