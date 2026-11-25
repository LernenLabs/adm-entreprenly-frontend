package online.entreprenly.entreprenlyapp.iam.interfaces.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import online.entreprenly.entreprenlyapp.EntreprenlyApplication
import online.entreprenly.entreprenlyapp.MainActivity
import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.PasswordResetLink
import online.entreprenly.entreprenlyapp.iam.interfaces.ui.viewmodels.PasswordResetViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.EntreprenlyAppTheme

class PasswordResetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        val container = (application as EntreprenlyApplication).container
        val link = PasswordResetLink.parse(intent.dataString)
        val viewModel by viewModels<PasswordResetViewModel> {
            viewModelFactory { initializer { PasswordResetViewModel(container.userCommandService, link) } }
        }
        setContent {
            EntreprenlyAppTheme {
                Surface {
                    PasswordResetScreen(viewModel) {
                        startActivity(Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                        finish()
                    }
                }
            }
        }
    }
}
