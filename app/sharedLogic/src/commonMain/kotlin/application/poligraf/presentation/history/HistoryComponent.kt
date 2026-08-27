package application.poligraf.presentation.history

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
interface HistoryComponent {
    val model: Value<Model>

    data class Model(
        val viewModel: HistoryViewModel,
        val backHandler: BackHandler
    )
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultHistoryComponent(
    componentContext: AppComponentContext,
    val navigateToDetail: (String) -> Unit,
    val navigateBack: () -> Unit,
) : HistoryComponent, AppComponentContext by componentContext, KoinComponent {

    private val historyViewModel = viewModel("historyViewModel") {
        HistoryViewModel(
            historyRepository = get(),
            navigateToDetail = navigateToDetail,
            navigateBack = navigateBack
        )
    }

    private val _model = MutableValue(
        HistoryComponent.Model(
            viewModel = historyViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<HistoryComponent.Model> = _model
}
