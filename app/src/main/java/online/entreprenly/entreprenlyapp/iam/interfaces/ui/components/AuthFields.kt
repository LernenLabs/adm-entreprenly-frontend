package online.entreprenly.entreprenlyapp.iam.interfaces.ui.components

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.FormState

@Composable
fun EmailField(value: String, onChange: (String) -> Unit, label: String = "Email") {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordField(value: String, onChange: (String) -> Unit, label: String = "Contraseña") {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TextInputField(value: String, onChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Botón de envío con spinner y mensajes de error/éxito del formulario. */
@Composable
fun FormSubmit(form: FormState, label: String, onClick: () -> Unit) {
    Column {
        form.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        form.success?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(onClick = onClick, enabled = !form.loading, modifier = Modifier.fillMaxWidth()) {
            if (form.loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text(label)
        }
    }
}
