package application.liedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.liedetector.navigation.NativeNavHost
import application.liedetector.navigation.navigationContext
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.root.RootComponent
import application.liedetector.theme.LieDetectorTheme
import application.liedetector.ui.components.background.ScalesBackground
import application.liedetector.ui.screens.main.MainHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val root = RootComponent(navigationContext())

        setContent {
            LieDetectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. Background layer - Reduced blur to keep cubes visible
                        ScalesBackground(
                            modifier = Modifier.blur(4.dp)
                        )
                        
                        // 2. Very subtle glass veil
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.15f))
                        )
                        
                        // 3. Navigation Host
                        NativeNavHost(root.navigator) { component ->
                            when (component) {
                                is MainComponent -> MainHost(component)
                                else -> Text("Loading...")
                            }
                        }
                    }
                }
            }
        }
    }
}
