package application.poligraf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.poligraf.engine.component.asAppComponentContext
import application.poligraf.presentation.App
import application.poligraf.presentation.root.DefaultRootComponent
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.asJetpackComponentContext
import com.arkivanov.decompose.retainedComponent

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext.asJetpackComponentContext()
                    .asAppComponentContext()
            )
        }
        
        setContent {
            App(
                root = root,
            )
        }
    }
}
