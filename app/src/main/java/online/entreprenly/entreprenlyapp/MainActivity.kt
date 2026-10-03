package online.entreprenly.entreprenlyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import online.entreprenly.entreprenlyapp.shared.interfaces.navigation.AppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as EntreprenlyApplication).container
        setContent { AppRoot(container) }
    }
}
