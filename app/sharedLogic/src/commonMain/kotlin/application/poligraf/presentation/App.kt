package application.poligraf.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import application.poligraf.presentation.root.RootComponent
import application.poligraf.presentation.theme.PoligrafTheme
import application.poligraf.presentation.main.MainContent
import application.poligraf.presentation.debug.DebugContent
import application.poligraf.uicore.common.backAnimation
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun App(root: RootComponent) {
    PoligrafTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val childStack by root.childStack.subscribeAsState()

            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize(),
                animation = backAnimation(
                    backHandler = when (val screen = childStack.active.instance) {
                        is RootComponent.Child.MainChild ->
                            screen.component.model.subscribeAsState().value.backHandler
                        is RootComponent.Child.DebugChild ->
                            screen.component.model.subscribeAsState().value.backHandler
                    },
                    onBack = {
                        when (val screen = childStack.active.instance) {
                            is RootComponent.Child.MainChild -> {}
                            is RootComponent.Child.DebugChild -> {
                                root.goBack()
                            }
                        }
                    }
                )
            ) { child ->
                when (val screen = child.instance) {
                    is RootComponent.Child.MainChild -> {
                        MainContent(screen.component)
                    }
                    is RootComponent.Child.DebugChild -> {
                        DebugContent(screen.component)
                    }
                }
            }
        }
    }
}
