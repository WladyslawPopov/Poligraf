package application.poligraf.presentation.main

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.AppComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Stable
interface MainComponent {
    val model: Value<Model>

    data class Model(
        val viewModel: MainViewModel,
        val backHandler: BackHandler
    )
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultMainComponent(
    componentContext: AppComponentContext,
    val navigateToDebug: () -> Unit,
) : MainComponent, AppComponentContext by componentContext, KoinComponent {

    private val mainViewModel = viewModel("mainViewModel") {
        MainViewModel(
            appConfig = get(),
            analyzerRepository = get(),
            navigateToDebug = navigateToDebug
        )
    }

    private val _model = MutableValue(
        MainComponent.Model(
            viewModel = mainViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<MainComponent.Model> = _model
}
