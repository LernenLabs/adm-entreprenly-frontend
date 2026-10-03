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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.EmailField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.FormSubmit
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PasswordField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.TextInputField
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignUpViewModel

/** US-56: sign up with email (the backend assigns the Free plan through events). */
@Composable
fun SignUpScreen(viewModel: SignUpViewModel, onGoToSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val form by viewModel.form.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(stringResource(R.string.sign_up_title), style = MaterialTheme.typography.headlineMedium)
        EmailField(email, { email = it })
        PasswordField(password, { password = it })
        TextInputField(firstName, { firstName = it }, stringResource(R.string.sign_up_first_name))
        TextInputField(lastName, { lastName = it }, stringResource(R.string.sign_up_last_name))
        TextInputField(phone, { phone = it }, stringResource(R.string.sign_up_phone), keyboardType = KeyboardType.Phone)
        FormSubmit(form, stringResource(R.string.sign_up_action), { viewModel.signUp(email, password, firstName, lastName, phone) })
        TextButton(onClick = onGoToSignIn) { Text(stringResource(R.string.sign_up_go_to_sign_in)) }
    }
}
