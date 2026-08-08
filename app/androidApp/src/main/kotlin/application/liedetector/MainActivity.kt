package application.liedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.liedetector.ui.app.App
import application.liedetector.ui.app.RootComponentHolder

class MainActivity : ComponentActivity() {

    private val componentHolder: RootComponentHolder by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            App(
                root = componentHolder.root,
                navigator = componentHolder.navigator
            )
        }
    }
}
