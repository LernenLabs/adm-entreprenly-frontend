package online.entreprenly.entreprenlyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.AppNavigation
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.EntreprenlyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as EntreprenlyApplication).container
        setContent {
            EntreprenlyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(container, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
