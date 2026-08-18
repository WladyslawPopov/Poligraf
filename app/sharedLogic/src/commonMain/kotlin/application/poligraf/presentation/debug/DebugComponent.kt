package application.poligraf.presentation.debug

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.AppComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import org.koin.core.component.KoinComponent

@Stable
interface DebugComponent {
    val model: Value<Model>

    data class Model(
        val viewModel: DebugViewModel,
        val backHandler: BackHandler
    )
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultDebugComponent(
    componentContext: AppComponentContext,
    val navigateBack: () -> Unit,
    val navigateToMain: () -> Unit
) : DebugComponent, AppComponentContext by componentContext, KoinComponent {

    // ViewModel is scoped to the Decompose component lifecycle
    private val debugViewModel = viewModel("debugViewModel") {
        DebugViewModel(
            navigateBack = navigateBack,
            navigateToMain = navigateToMain
        )
    }

    private val _model = MutableValue(
        DebugComponent.Model(
            viewModel = debugViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<DebugComponent.Model> = _model
}
