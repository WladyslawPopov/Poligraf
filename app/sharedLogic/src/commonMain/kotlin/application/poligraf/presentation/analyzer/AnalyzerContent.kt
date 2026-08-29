package application.poligraf.presentation.analyzer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import application.poligraf.presentation.analyzer.ui.AnalyzerRenderer
import application.poligraf.ui.foundation.actions.RecordingAction
import application.poligraf.ui.components.layout.AppScaffold
import application.poligraf.ui.components.layout.StandardToolbar
import application.poligraf.ui.theme.LocalDesignSystem
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun AnalyzerContent(component: AnalyzerComponent) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

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
                    durationText = state.durationText,
                    isRecording = state.isRecording && !state.isPaused,
                    isProcessing = state.isProcessing,
                    showIndicator = true,
                    onAction = { action ->
                        viewModel.onAction(action)
                        if (action is RecordingAction.Delete) {
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
