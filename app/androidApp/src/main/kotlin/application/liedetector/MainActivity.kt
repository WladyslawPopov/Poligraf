package application.liedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.liedetector.navigation.NativeNavHost
import application.liedetector.navigation.navigationContext
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.root.RootComponent
import application.liedetector.theme.LieDetectorTheme
import application.liedetector.ui.components.background.ScalesBackground
import application.liedetector.ui.screens.main.MainHost
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.ui.screens.drawer.MainDrawer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LieDetectorTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val designSystem = LocalDesignSystem.current

                val root = remember {
                    RootComponent(navigationContext()) {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                }

                NativeNavHost(
                    navigator = root.navigator,
                    drawerState = drawerState,
                    background = { ScalesBackground() },
                    drawerContent = { MainDrawer(designSystem) }
                ) { component ->
                    when (component) {
                        is MainComponent -> MainHost(component)
                        else -> {}
                    }
                }
            }
        }
    }
}
