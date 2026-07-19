package application.liedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.liedetector.navigation.navigationContext
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.root.RootComponent
import application.liedetector.presentation.root.AppRoute
import application.liedetector.theme.LieDetectorTheme
import application.liedetector.ui.screens.main.MainHost
import application.liedetector.ui.screens.debug.DebugHost
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.ui.screens.drawer.MainDrawer

class MainActivity : ComponentActivity(), AppNavigation {

    private val navigationStack = mutableStateListOf<AppRoute>(AppRoute.Main)
    private var rootComponent: RootComponent? = null

    override fun openMain() {
        navigationStack.clear()
        navigationStack.add(AppRoute.Main)
    }

    override fun openDebug() {
        navigationStack.add(AppRoute.Debug)
    }

    override fun openInvestigation(subjectId: String) {
        navigationStack.add(AppRoute.Investigation(subjectId))
    }

    override fun back() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
        }
    }

    override fun toggleDrawer() {
        rootComponent?.toggleDrawer()
    }

    override fun setDrawerOpen(isOpen: Boolean) {
        rootComponent?.setDrawerOpen(isOpen)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LieDetectorTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val designSystem = LocalDesignSystem.current

                val root = remember { 
                    RootComponent(navigationContext(), this).also { rootComponent = it } 
                }

                // Sync drawer state between Command and Compose
                val isDrawerOpen by root.isDrawerOpen.collectAsState()
                
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
                    if (isOpen != root.isDrawerOpen.value) {
                        root.setDrawerOpen(isOpen)
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = { MainDrawer(designSystem, this) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    val topRoute = navigationStack.last()
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (topRoute) {
                            is AppRoute.Main -> {
                                val component = remember(topRoute) { root.createMainComponent(navigationContext()) }
                                MainHost(component)
                            }
                            is AppRoute.Debug -> {
                                val component = remember(topRoute) { root.createDebugComponent(navigationContext()) }
                                DebugHost(component)
                            }
                            else -> {
                                Text("Route not implemented: $topRoute", modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
                
                BackHandler(enabled = navigationStack.size > 1 || drawerState.isOpen) {
                    if (drawerState.isOpen) {
                        setDrawerOpen(false)
                    } else {
                        back()
                    }
                }
            }
        }
    }
}
