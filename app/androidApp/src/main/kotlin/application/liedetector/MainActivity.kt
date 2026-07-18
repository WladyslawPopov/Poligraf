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
import application.liedetector.ui.screens.main.MainHost
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.ui.screens.drawer.MainDrawer

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LieDetectorTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val designSystem = LocalDesignSystem.current

                val root = remember { RootComponent(navigationContext()) }

                // Sync drawer state between Navigator and Compose
                val isDrawerOpen by root.navigator.isDrawerOpen.collectAsState()
                
                LaunchedEffect(isDrawerOpen) {
                    if (isDrawerOpen && drawerState.isClosed) {
                        drawerState.open()
                    } else if (!isDrawerOpen && drawerState.isOpen) {
                        drawerState.close()
                    }
                }

                // Sync back when drawer is closed by swipe/tap
                LaunchedEffect(drawerState.currentValue) {
                    val isOpen = drawerState.currentValue == DrawerValue.Open
                    if (isOpen != root.navigator.isDrawerOpen.value) {
                        root.navigator.setDrawerOpen(isOpen)
                    }
                }

                NativeNavHost(
                    navigator = root.navigator,
                    drawerState = drawerState,
                    drawerContent = { MainDrawer(designSystem) },
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
