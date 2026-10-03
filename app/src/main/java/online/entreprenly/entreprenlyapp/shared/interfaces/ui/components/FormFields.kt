package online.entreprenly.entreprenlyapp.shared.interfaces.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState

/** Reusable form building blocks so every module looks and behaves the same. */

@Composable
fun TextInputField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun EmailField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.field_email)
) = TextInputField(value, onChange, label, modifier, KeyboardType.Email)

@Composable
fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.field_password)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = modifier.fillMaxWidth()
    )
}

/** Submit button with spinner plus the form's error/success message. */
@Composable
fun FormSubmit(form: FormState, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        form.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        form.success?.let { Text(it.asString(), color = MaterialTheme.colorScheme.primary) }
        Button(onClick = onClick, enabled = !form.loading, modifier = Modifier.fillMaxWidth()) {
            if (form.loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text(label)
        }
    }
}
