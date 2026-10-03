package online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.EmailField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.FormSubmit
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PasswordField
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.AccountViewModel

/** US-66 change password, US-65 change email and US-61 sign out. */
@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    email: String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordForm by viewModel.passwordForm.collectAsState()
    val emailForm by viewModel.emailForm.collectAsState()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var newEmail by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.account_title), style = MaterialTheme.typography.headlineMedium)
        Text(email, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider()
        Text(stringResource(R.string.account_change_password_title), style = MaterialTheme.typography.titleMedium)
        PasswordField(currentPassword, { currentPassword = it }, label = stringResource(R.string.account_current_password))
        PasswordField(newPassword, { newPassword = it }, label = stringResource(R.string.account_new_password))
        FormSubmit(
            passwordForm,
            stringResource(R.string.account_change_password_action),
            { viewModel.changePassword(currentPassword, newPassword) }
        )

        HorizontalDivider()
        Text(stringResource(R.string.account_change_email_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.account_change_email_hint), style = MaterialTheme.typography.bodySmall)
        EmailField(newEmail, { newEmail = it }, label = stringResource(R.string.account_new_email))
        FormSubmit(emailForm, stringResource(R.string.account_change_email_action), { viewModel.changeEmail(newEmail) })

        HorizontalDivider()
        OutlinedButton(onClick = onSignOut) { Text(stringResource(R.string.account_sign_out)) }
    }
}
