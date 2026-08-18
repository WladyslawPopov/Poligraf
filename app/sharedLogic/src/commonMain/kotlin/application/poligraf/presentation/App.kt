package application.poligraf.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import application.poligraf.data.AppRoute
import application.poligraf.navigation.SharedNavigator
import application.poligraf.presentation.root.RootComponent
import application.poligraf.presentation.theme.PoligrafTheme
import application.poligraf.presentation.screens.MainHost
import application.poligraf.presentation.screens.DebugHost
import application.poligraf.presentation.screens.RecordingHost
import application.poligraf.presentation.screens.RecordingsHistoryHost

@Composable
fun App(
    root: RootComponent,
    navigator: SharedNavigator
) {
    PoligrafTheme {
        val currentRoute by navigator.currentRoute.collectAsState()
        
        AnimatedContent(
            targetState = currentRoute,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { route ->
            when (route) {
                is AppRoute.Main -> {
                    MainHost(
                        component = root.mainComponent(root.context.childContext("main_screen")),
                        navigator = navigator
                    )
                }
                is AppRoute.Debug -> {
                    DebugHost(
                        component = root.debugComponent(root.context.childContext("debug_screen"))
                    )
                }
                is AppRoute.Recording -> {
                    RecordingHost(
                        component = root.recordingComponent(root.context.childContext("recording_${route.subjectId}"), route.subjectId)
                    )
                }
                is AppRoute.RecordingsHistory -> {
                    RecordingsHistoryHost(
                        component = root.recordingsHistoryComponent(root.context.childContext("history_${route.subjectId}"), route.subjectId, route.startRecording)
                    )
                }
            }
        }
    }
}
