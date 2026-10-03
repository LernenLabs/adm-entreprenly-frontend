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
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.components.EmailField
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.components.FormSubmit
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.components.PasswordField
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.AccountViewModel

/** US-66 cambiar contraseña, US-65 cambiar email y US-61 cerrar sesión. */
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
        Text("Mi cuenta", style = MaterialTheme.typography.headlineMedium)
        Text(email, style = MaterialTheme.typography.bodyLarge)

        HorizontalDivider()
        Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium)
        PasswordField(currentPassword, { currentPassword = it }, "Contraseña actual")
        PasswordField(newPassword, { newPassword = it }, "Nueva contraseña")
        FormSubmit(passwordForm, "Actualizar contraseña") {
            viewModel.changePassword(currentPassword, newPassword)
        }

        HorizontalDivider()
        Text("Cambiar email", style = MaterialTheme.typography.titleMedium)
        Text("Al cambiarlo deberás iniciar sesión nuevamente.", style = MaterialTheme.typography.bodySmall)
        EmailField(newEmail, { newEmail = it }, "Nuevo email")
        FormSubmit(emailForm, "Actualizar email") { viewModel.changeEmail(newEmail) }

        HorizontalDivider()
        OutlinedButton(onClick = onSignOut) { Text("Cerrar sesión") }
    }
}
