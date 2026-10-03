package online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.components.PasswordRequirements
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.components.PasswordRules
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignUpViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.LabeledTextField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.LinkText
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PlainTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/** US-56: sign up with email (the backend assigns the Free plan through events). */
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onBack: () -> Unit,
    onGoToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val form by viewModel.form.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    val canSubmit = email.isNotBlank() && PasswordRules.of(password).allMet && !form.loading

    Column(modifier.fillMaxSize().imePadding()) {
        PlainTopBar(stringResource(R.string.sign_up_title), onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                stringResource(R.string.sign_up_subtitle),
                color = MaterialTheme.extraColors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
            LabeledTextField(
                label = stringResource(R.string.field_email),
                value = email,
                onChange = { email = it },
                keyboardType = KeyboardType.Email,
                trailingIcon = {
                    Icon(Icons.Outlined.MailOutline, contentDescription = null, tint = MaterialTheme.extraColors.muted)
                },
                isError = form.error != null,
                errorText = form.error?.asString()
            )
            LabeledTextField(
                label = stringResource(R.string.field_password),
                value = password,
                onChange = { password = it },
                isPassword = true
            )
            PasswordRequirements(password)
            LabeledTextField(stringResource(R.string.sign_up_first_name), firstName, { firstName = it })
            LabeledTextField(stringResource(R.string.sign_up_last_name), lastName, { lastName = it })
            LabeledTextField(
                stringResource(R.string.sign_up_phone), phone, { phone = it }, keyboardType = KeyboardType.Phone
            )
        }
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryPillButton(
                stringResource(R.string.sign_up_action),
                onClick = { viewModel.signUp(email, password, firstName, lastName, phone) },
                enabled = canSubmit
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.auth_have_account), color = MaterialTheme.extraColors.muted)
                LinkText(stringResource(R.string.auth_sign_in_link), onGoToSignIn)
            }
        }
    }
}
