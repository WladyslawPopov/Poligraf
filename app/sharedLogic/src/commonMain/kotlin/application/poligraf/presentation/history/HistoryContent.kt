package application.poligraf.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import application.poligraf.presentation.history.ui.HistoryListRenderer
import application.poligraf.presentation.history.ui.HistoryToolbar
import application.poligraf.ui.components.layout.AppScaffold
import application.poligraf.ui.foundation.state.ScaffoldUiState
import application.poligraf.ui.theme.LocalDesignSystem
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun HistoryContent(component: HistoryComponent) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    AppScaffold(
        viewModel = viewModel,
        state = state as ScaffoldUiState,
        topBar = {
            state.toolbar?.let { toolbar ->
                HistoryToolbar(
                    toolbar = toolbar,
                    designSystem = designSystem,
                    onBack = viewModel::onBack
                )
            }
        }
    ) { padding ->
        HistoryListRenderer(
            state = state,
            onSessionClick = viewModel::onSessionClick,
            onDeleteSession = viewModel::onDeleteSession,
            contentPadding = padding
        )
    }
}
