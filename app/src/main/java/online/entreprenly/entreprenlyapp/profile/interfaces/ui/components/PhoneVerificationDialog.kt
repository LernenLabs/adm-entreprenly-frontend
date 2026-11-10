package online.entreprenly.entreprenlyapp.profile.interfaces.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.FormSubmit

@Composable
fun PhoneVerificationDialog(viewModel: ProfileViewModel, currentPhone: String, onClose: () -> Unit) {
    val state by viewModel.phoneVerification.collectAsState()
    var phone by rememberSaveable { mutableStateOf(currentPhone) }
    var code by remember(state.sentTo) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!state.form.loading) onClose() },
        title = { Text(stringResource(R.string.profile_phone_verify)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val sentTo = state.sentTo
                when {
                    state.verified -> Text(stringResource(R.string.profile_phone_verified))
                    sentTo == null -> {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(stringResource(R.string.profile_phone_title)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            enabled = !state.form.loading
                        )
                        FormSubmit(state.form, stringResource(R.string.profile_phone_send), {
                            viewModel.requestPhoneVerification(phone)
                        })
                    }
                    else -> {
                        Text(stringResource(R.string.profile_phone_destination, sentTo.value))
                        OutlinedTextField(
                            value = code,
                            onValueChange = { if (it.length <= 6) code = it },
                            label = { Text(stringResource(R.string.profile_phone_code)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !state.form.loading
                        )
                        FormSubmit(state.form, stringResource(R.string.profile_phone_verify), {
                            viewModel.confirmPhoneVerification(code)
                        })
                        TextButton(
                            onClick = {
                                code = ""
                                viewModel.requestPhoneVerification(sentTo.value)
                            },
                            enabled = !state.form.loading
                        ) { Text(stringResource(R.string.profile_phone_resend)) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose, enabled = !state.form.loading) {
                Text(stringResource(R.string.profile_phone_close))
            }
        }
    )
}
