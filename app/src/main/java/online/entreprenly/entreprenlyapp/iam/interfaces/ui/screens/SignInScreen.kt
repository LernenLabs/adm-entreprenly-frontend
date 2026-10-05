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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.SignInViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.ErrorBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.LabeledTextField
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.LinkText
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PlainTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

/** US-58: sign in with credentials. */
@Composable
fun SignInScreen(
    viewModel: SignInViewModel,
    onBack: () -> Unit,
    onGoToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val form by viewModel.form.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(modifier.fillMaxSize().imePadding()) {
        PlainTopBar(stringResource(R.string.sign_in_title), onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                stringResource(R.string.sign_in_welcome_back),
                color = MaterialTheme.extraColors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
            form.errorTitle?.let { ErrorBanner(it.asString(), form.error?.asString()) }
            LabeledTextField(
                label = stringResource(R.string.field_email),
                value = email,
                onChange = { email = it },
                keyboardType = KeyboardType.Email,
                trailingIcon = {
                    Icon(Icons.Outlined.MailOutline, contentDescription = null, tint = MaterialTheme.extraColors.muted)
                },
                isError = form.errorTitle != null
            )
            LabeledTextField(
                label = stringResource(R.string.field_password),
                value = password,
                onChange = { password = it },
                isPassword = true,
                isError = form.errorTitle != null
            )
            if (form.errorTitle == null) {
                form.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
            }
        }
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryPillButton(
                stringResource(R.string.sign_in_action),
                onClick = { viewModel.signIn(email, password) },
                enabled = !form.loading
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.auth_no_account), color = MaterialTheme.extraColors.muted)
                LinkText(stringResource(R.string.auth_sign_up_link), onGoToSignUp)
            }
        }
    }
}
