package application.poligraf.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import application.poligraf.engine.component.asAppComponentContext
import application.poligraf.presentation.root.DefaultRootComponent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.decompose.jetpackcomponentcontext.asJetpackComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import platform.UIKit.UIViewController

@OptIn(ExperimentalDecomposeApi::class)
class RootAppController {

    private val backDispatcher = BackDispatcher()

    // Initialize RootComponent using our Engine's generic wrapper
    private val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(
            lifecycle = ApplicationLifecycle(),
            backHandler = backDispatcher
        ).asJetpackComponentContext().asAppComponentContext()
    )

    fun rootViewController(): UIViewController = ComposeUIViewController {
        PredictiveBackGestureOverlay(
            backDispatcher = backDispatcher,
            backIcon = { progress, _ ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .alpha((progress * 6F).coerceAtMost(1F))
                        .background(color = Color.LightGray, shape = CircleShape)
                        .padding(4.dp),
                    tint = Color.Unspecified,
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            App(rootComponent)
        }
    }
}
