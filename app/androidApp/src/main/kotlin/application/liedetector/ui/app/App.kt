package application.liedetector.ui.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import application.liedetector.data.AppRoute
import application.liedetector.navigation.AndroidNavigator
import application.liedetector.navigation.NavEvent
import application.liedetector.engine.component.componentContext
import application.liedetector.presentation.root.RootComponent
import application.liedetector.theme.LieDetectorTheme
import application.liedetector.ui.screens.debug.DebugHost
import application.liedetector.ui.screens.main.MainHost
import application.liedetector.ui.screens.recording.RecordingHost
import application.liedetector.ui.screens.recordingHistory.RecordingsHistoryHost
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun App(
    root: RootComponent,
    navigator: AndroidNavigator
) {
    LieDetectorTheme {
        val navController = rememberNavController()

        // Sync shared navigator with Android NavController
        LaunchedEffect(navController) {
            navigator.navigationEvents
                .onEach { event ->
                    handleNavigationEvent(navController, event)
                }
                .launchIn(this)
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
            composable<AppRoute.Main> { backStackEntry ->
                MainHost(
                    component = root.mainComponent(backStackEntry.componentContext()),
                    navigator = navigator
                )
            }
            composable<AppRoute.Debug> { backStackEntry ->
                DebugHost(
                    component = root.debugComponent(backStackEntry.componentContext())
                )
            }
            composable<AppRoute.Recording> { backStackEntry ->
                val route = backStackEntry.toRoute<AppRoute.Recording>()
                RecordingHost(
                    component = root.recordingComponent(backStackEntry.componentContext(), route.subjectId)
                )
            }
            composable<AppRoute.RecordingsHistory> { backStackEntry ->
                val route = backStackEntry.toRoute<AppRoute.RecordingsHistory>()
                RecordingsHistoryHost(
                    component = root.recordingsHistoryComponent(backStackEntry.componentContext(), route.subjectId, route.startRecording)
                )
            }
        }
    }
}

private fun handleNavigationEvent(navController: NavController, event: NavEvent) {
    when (event) {
        is NavEvent.OpenMain -> {
            navController.navigate(AppRoute.Main) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        is NavEvent.OpenDebug -> {
            navController.navigate(AppRoute.Debug) {
                launchSingleTop = true
            }
        }
        is NavEvent.OpenRecording -> {
            navController.navigate(AppRoute.Recording(event.subjectId)) {
                launchSingleTop = true
            }
        }
        is NavEvent.OpenRecordingsHistory -> {
            navController.navigate(AppRoute.RecordingsHistory(event.subjectId, event.startRecording)) {
                launchSingleTop = true
            }
        }
        is NavEvent.Back -> {
            navController.popBackStack()
        }
    }
}
