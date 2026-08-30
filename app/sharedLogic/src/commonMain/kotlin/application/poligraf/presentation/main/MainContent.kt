package application.poligraf.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import application.poligraf.presentation.main.ui.MainBottomSheet
import application.poligraf.ui.components.layout.AppScaffold
import application.poligraf.ui.components.layout.StandardToolbar
import application.poligraf.ui.features.main.components.MainAnalyzeBtn
import application.poligraf.ui.features.main.components.MainWelcomeText
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent,
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
                StandardToolbar(
                    toolbar = toolbar,
                    designSystem = designSystem,
                    onAction = viewModel::onAction,
                    isTyping = true
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            state.welcomeWidget?.let {
                MainWelcomeText(it)
            }

            Spacer(Modifier.weight(1f))

            state.analyzeBtn?.let {
                MainAnalyzeBtn(
                    model = it,
                    onClick = viewModel::onAnalyzeClick
                )
            }
            
            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_XL)))
        }

        MainBottomSheet(
            modifier = Modifier.navigationBarsPadding(),
            state = state,
            defaultSkin = viewModel.defaultSkin,
            markerShape = viewModel.markerShape,
            onSkinSelected = viewModel::onSkinSelected,
            onMarkerShapeSelected = viewModel::onMarkerShapeSelected,
            designSystem = designSystem,
            onDebugClicked = { viewModel.onDebugClicked() },
            closeBottomSheet = { viewModel.closeBottomSheet() }
        )
    }
}
