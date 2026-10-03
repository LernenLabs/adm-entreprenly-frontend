package online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileStateGate
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/** US-63: edit first name, last name and biography. A blank first name blocks saving. */
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val form by viewModel.editForm.collectAsState()

    DisposableEffect(Unit) { onDispose { viewModel.resetEditForm() } }

    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.edit_title), onBack)
        ProfileStateGate(state, onRetry = viewModel::load, onSignIn = onSignIn) { profile ->
            var firstName by rememberSaveable { mutableStateOf(profile.firstName) }
            var lastName by rememberSaveable { mutableStateOf(profile.lastName) }
            var biography by rememberSaveable { mutableStateOf(profile.biography.orEmpty()) }
            val firstNameMissing = firstName.isBlank()

            Column(Modifier.fillMaxSize().padding(14.dp)) {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        stringResource(R.string.edit_intro),
                        color = MaterialTheme.extraColors.muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(stringResource(R.string.edit_first_name)) },
                        singleLine = true,
                        isError = firstNameMissing,
                        supportingText = if (firstNameMissing) {
                            { Text(stringResource(R.string.edit_first_name_required)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(stringResource(R.string.edit_last_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = biography,
                        onValueChange = { if (it.length <= 500) biography = it },
                        label = { Text(stringResource(R.string.edit_biography)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        stringResource(R.string.edit_hint),
                        color = MaterialTheme.extraColors.muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (form.error != null && !firstNameMissing) {
                        Text(form.error!!.asString(), color = MaterialTheme.colorScheme.error)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
                    PrimaryPillButton(
                        stringResource(R.string.edit_save),
                        onClick = { viewModel.updateProfile(firstName, lastName, biography, onSaved) },
                        enabled = !form.loading && firstName.isNotBlank()
                    )
                    SecondaryPillButton(stringResource(R.string.edit_cancel), onBack)
                }
            }
        }
    }
}
