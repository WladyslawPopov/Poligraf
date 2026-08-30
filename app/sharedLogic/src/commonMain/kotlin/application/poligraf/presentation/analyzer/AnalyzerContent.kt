package application.poligraf.presentation.analyzer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import application.poligraf.domain.model.AnalyzerMode
import application.poligraf.presentation.analyzer.ui.AnalyzerRenderer
import application.poligraf.ui.components.layout.AppScaffold
import application.poligraf.ui.components.layout.StandardToolbar
import application.poligraf.ui.foundation.actions.AnalyzingAction
import application.poligraf.ui.theme.LocalDesignSystem
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun AnalyzerContent(component: AnalyzerComponent) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current
    val isLive = state.mode == AnalyzerMode.LIVE

    LaunchedEffect(viewModel) {
        viewModel.navigateToDetail.collect { sessionId ->
            component.navigateToDetail(sessionId)
        }
    }

    AppScaffold(
        viewModel = viewModel,
        state = state,
        topBar = {
            state.toolbar?.let { toolbar ->
                StandardToolbar(
                    toolbar = toolbar,
                    designSystem = designSystem,
                    onNavigationClick = component::onBack,
                    durationText = if (isLive) state.durationText else null,
                    isAnalyzing = if (isLive) state.isAnalyzing && !state.isPaused else false,
                    isProcessing = state.isProcessing,
                    showIndicator = isLive,
                    onAction = { action ->
                        viewModel.onAction(action)
                        if (action is AnalyzingAction.Delete) {
                            component.onBack()
                        }
                    }
                )
            }
        }
    ) { padding ->
        AnalyzerRenderer(
            viewModel = viewModel,
            contentPadding = padding
        )
    }
}

