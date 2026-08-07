package application.liedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import application.liedetector.component.componentContext
import application.liedetector.navigation.AndroidNavigator
import application.liedetector.presentation.root.RootComponent
import application.liedetector.navigation.AppRoute
import application.liedetector.theme.LieDetectorTheme
import application.liedetector.ui.screens.main.MainHost
import application.liedetector.ui.screens.debug.DebugHost
import application.liedetector.ui.screens.recording.RecordingHost
import application.liedetector.uicore.theme.LocalDesignSystem

class MainActivity : ComponentActivity() {

    private val navigator = AndroidNavigator()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LieDetectorTheme {
                val designSystem = LocalDesignSystem.current
                val navController = rememberNavController()

                // Bind navigator to the lifecycle of this NavHost
                LaunchedEffect(navController) {
                    navigator.bind(navController)
                }

                val root = remember { 
                    RootComponent(componentContext(), navigator)
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Main,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(400)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(400)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(400)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(400)
                        )
                    }
                ) {
                    composable<AppRoute.Main> {
                        MainHost(root.mainComponent, navigator)
                    }
                    composable<AppRoute.Debug> {
                        DebugHost(root.debugComponent)
                    }
                    composable<AppRoute.Recording> { backStackEntry ->
                        val route: AppRoute.Recording = backStackEntry.arguments?.let { 
                            // In Type-safe navigation 2.8.0+, we can use backStackEntry.toRoute()
                            // but let's stick to the root factory for now.
                            AppRoute.Recording(it.getString("subjectId") ?: "")
                        } ?: AppRoute.Recording("")

                        val component = root.createRecordingComponent(route.subjectId)
                        RecordingHost(component)
                    }
                }
            }
        }
    }
}
