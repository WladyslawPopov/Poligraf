package application.poligraf.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import application.poligraf.presentation.main.ui.MainBottomSheet
import application.poligraf.presentation.main.ui.MainToolbar
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.features.render.WidgetRenderer
import application.poligraf.ui.components.layout.AppScaffold
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent
) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    AppScaffold(
        viewModel = viewModel,
        state = state,
        topBar = {
            state.toolbar?.let { toolbar ->
                MainToolbar(
                    toolbar = toolbar,
                    designSystem = designSystem,
                    onWidgetAction = viewModel::onWidgetAction
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = padding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_MEDIUM))
        ) {
            items(state.widgets) { item ->
                WidgetRenderer(
                    item,
                    onAction = viewModel::onWidgetAction
                )
            }
        }

        MainBottomSheet(
            modifier = Modifier.navigationBarsPadding(),
            state = state,
            analyzerViewModel = componentModel.analyzerViewModel,
            designSystem = designSystem,
            onDebugClicked = { viewModel.onDebugClicked() },
            closeBottomSheet = { viewModel.closeBottomSheet() }
        )
    }
}
