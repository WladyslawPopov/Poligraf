package application.poligraf.presentation.analyzer

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
interface AnalyzerComponent {
    val model: Value<Model>
    fun onBack()
    fun navigateToDetail(sessionId: String)

    data class Model(
        val viewModel: AnalyzerViewModel,
        val backHandler: BackHandler,
    )
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAnalyzerComponent(
    componentContext: AppComponentContext,
    private val onNavigateToDetail: (String) -> Unit,
    private val onNavigateBack: () -> Unit,
) : AnalyzerComponent, AppComponentContext by componentContext, KoinComponent {

    private val analyzerViewModel = viewModel("analyzerViewModel") {
        AnalyzerViewModel(
            repository = get(),
            historyRepository = get(),
            preferencesRepository = get(),
        )
    }

    private val _model = MutableValue(
        AnalyzerComponent.Model(
            viewModel = analyzerViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<AnalyzerComponent.Model> = _model

    override fun onBack() {
        analyzerViewModel.onBack()
        onNavigateBack()
    }

    override fun navigateToDetail(sessionId: String) {
        onNavigateToDetail(sessionId)
    }
}
