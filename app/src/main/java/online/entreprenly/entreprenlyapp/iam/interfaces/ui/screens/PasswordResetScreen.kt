package online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.PasswordResetViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.EmailField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.FormSubmit
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PasswordField

@Composable
fun PasswordRecoveryDialog(viewModel: PasswordResetViewModel, onClose: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val form by viewModel.form.collectAsState()
    AlertDialog(
        onDismissRequest = { if (!form.loading) onClose() },
        title = { Text(stringResource(R.string.reset_request_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EmailField(email, { email = it }, label = stringResource(R.string.field_email))
                FormSubmit(form, stringResource(R.string.reset_send_link), { viewModel.request(email) })
            }
        },
        confirmButton = {
            TextButton(onClick = onClose, enabled = !form.loading) { Text(stringResource(R.string.profile_phone_close)) }
        }
    )
}

@Composable
fun PasswordResetScreen(viewModel: PasswordResetViewModel, onReturn: () -> Unit) {
    val form by viewModel.form.collectAsState()
    val ready by viewModel.linkReady.collectAsState()
    val completed by viewModel.completed.collectAsState()

    LaunchedEffect(viewModel) { viewModel.validateLink() }
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.reset_password_title), style = MaterialTheme.typography.headlineSmall)
        when {
            completed -> Text(stringResource(R.string.reset_password_updated))
            ready -> {
                var password by remember { mutableStateOf("") }
                var confirmation by remember { mutableStateOf("") }
                PasswordField(password, { password = it }, label = stringResource(R.string.account_new_password))
                PasswordField(confirmation, { confirmation = it }, label = stringResource(R.string.account_confirm_password))
                FormSubmit(form, stringResource(R.string.reset_save_password), { viewModel.confirm(password, confirmation) })
            }
            form.loading -> CircularProgressIndicator()
            else -> form.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        }
        TextButton(onClick = onReturn, enabled = !form.loading) { Text(stringResource(R.string.reset_return_to_app)) }
    }
}
