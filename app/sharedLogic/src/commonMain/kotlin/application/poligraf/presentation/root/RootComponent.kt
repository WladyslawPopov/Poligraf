package application.poligraf.presentation.root

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.AppComponentContext
import application.poligraf.engine.component.asAppComponentContext
import application.poligraf.presentation.analyzer.AnalyzerComponent
import application.poligraf.presentation.analyzer.DefaultAnalyzerComponent
import application.poligraf.presentation.debug.DebugComponent
import application.poligraf.presentation.debug.DefaultDebugComponent
import application.poligraf.presentation.history.DefaultHistoryComponent
import application.poligraf.presentation.history.HistoryComponent
import application.poligraf.presentation.main.DefaultMainComponent
import application.poligraf.presentation.main.MainComponent
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import org.koin.core.component.KoinComponent

@Stable
interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    val backHandler: BackHandler

    sealed class Child {
        class MainChild(val component: MainComponent) : Child()
        class DebugChild(val component: DebugComponent) : Child()
        class HistoryChild(val component: HistoryComponent) : Child()
        class AnalyzerChild(val component: AnalyzerComponent) : Child()
    }

    fun goBack()
}

class DefaultRootComponent(
    componentContext: AppComponentContext
) : RootComponent, AppComponentContext by componentContext, KoinComponent
{
    private val navigation = StackNavigation<RootConfig>()

    @OptIn(ExperimentalDecomposeApi::class)
    override val childStack: Value<ChildStack<RootConfig, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = RootConfig.serializer(),
        initialConfiguration = RootConfig.Main,
        handleBackButton = true,
        childFactory = ::createChild
    )

    @OptIn(ExperimentalDecomposeApi::class)
    private fun createChild(config: RootConfig, context: JetpackComponentContext): RootComponent.Child {
        // Upgrade standard ComponentContext to our App Engine Context
        val appContext = context.asAppComponentContext()

        return when (config) {
            is RootConfig.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext = appContext,
                    navigateToDebug = { navigation.pushNew(RootConfig.Debug) },
                    navigateToHistory = { navigation.pushNew(RootConfig.History) },
                    navigateToAnalyzer = { navigation.pushNew(RootConfig.Analyzer()) }
                )
            )
            is RootConfig.Debug -> RootComponent.Child.DebugChild(
                DefaultDebugComponent(
                    componentContext = appContext,
                    navigateToMain = { navigation.pop() },
                    navigateBack = { navigation.pop() }
                )
            )
            is RootConfig.History -> RootComponent.Child.HistoryChild(
                DefaultHistoryComponent(
                    componentContext = appContext,
                    navigateToDetail = { navigation.pushNew(RootConfig.Analyzer(sessionId = it)) },
                    navigateBack = { navigation.pop() }
                )
            )
            is RootConfig.Analyzer -> RootComponent.Child.AnalyzerChild(
                DefaultAnalyzerComponent(
                    componentContext = appContext,
                    sessionId = config.sessionId,
                    onNavigateToDetail = { sessionId ->
                        // Replace live Analyzer with review Analyzer on the stack
                        navigation.pop()
                        navigation.pushNew(RootConfig.Analyzer(sessionId = sessionId))
                    },
                    onNavigateBack = { navigation.pop() }
                )
            )
        }
    }

    override fun goBack() {
        navigation.pop()
    }
}


