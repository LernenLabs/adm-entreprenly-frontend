package online.entreprenly.entreprenlyapp.shared

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.WithLanguage
import org.junit.Rule
import org.junit.Test

class ProfileActivityResultTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun savedLanguageKeepsActivityResultRegistrationAvailable() {
        compose.setContent {
            WithLanguage("es") {
                rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { }
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
                Text("Perfil")
            }
        }

        compose.onNodeWithText("Perfil").assertIsDisplayed()
    }
}
