package online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignInViewModel

/** US-58: sign in with credentials. */
@Composable
fun SignInScreen(viewModel: SignInViewModel, onGoToSignUp: () -> Unit, modifier: Modifier = Modifier) {
    val form by viewModel.form.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterVertically)
    ) {
        Text("Entreprenly", style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.sign_in_title), style = MaterialTheme.typography.titleMedium)
        EmailField(email, { email = it })
        PasswordField(password, { password = it })
        FormSubmit(form, stringResource(R.string.sign_in_action), { viewModel.signIn(email, password) })
        TextButton(onClick = onGoToSignUp) { Text(stringResource(R.string.sign_in_go_to_sign_up)) }
    }
}
