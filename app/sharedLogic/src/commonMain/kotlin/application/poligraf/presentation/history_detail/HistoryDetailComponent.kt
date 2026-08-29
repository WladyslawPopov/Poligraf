package application.poligraf.presentation.history_detail

import androidx.compose.runtime.Stable
import application.poligraf.presentation.history_detail.HistoryDetailViewModel
import application.poligraf.engine.component.AppComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Stable
interface HistoryDetailComponent {
    val model: Value<Model>

    data class Model(
        val viewModel: HistoryDetailViewModel,
        val backHandler: BackHandler,
    )
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultHistoryDetailComponent(
    componentContext: AppComponentContext,
    val sessionId: String,
    val navigateBack: () -> Unit,
) : HistoryDetailComponent, AppComponentContext by componentContext, KoinComponent {

    private val historyDetailViewModel = viewModel("historyDetailViewModel_$sessionId") {
        HistoryDetailViewModel(
            sessionId = sessionId,
            historyRepository = get(),
            analyzerRepository = get(),
            preferencesRepository = get(),
            navigateBack = navigateBack
        )
    }

    private val _model = MutableValue(
        HistoryDetailComponent.Model(
            viewModel = historyDetailViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<HistoryDetailComponent.Model> = _model
}
