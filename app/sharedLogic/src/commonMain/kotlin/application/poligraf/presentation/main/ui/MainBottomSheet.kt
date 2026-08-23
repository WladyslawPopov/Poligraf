package application.poligraf.presentation.main.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.presentation.main.data.MainBottomSheetContent
import application.poligraf.presentation.main.data.MainState
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.foundation.models.UiWidget
import application.poligraf.ui.components.layout.AppBottomSheet
import application.poligraf.ui.features.recorder.AnalyzerRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSheet(
    modifier: Modifier = Modifier,
    state: MainState,
    designSystem: DesignSystem,
    onDebugClicked: () -> Unit,
    closeBottomSheet: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (state.bottomSheetState) {
        AppBottomSheet(
            sheetState = bottomSheetState,
            designSystem = designSystem,
            modifier = modifier,
            onDismissRequest = closeBottomSheet
        ) {
            when (state.bottomSheetContent) {
                MainBottomSheetContent.SETTINGS -> SettingsContent(
                    appVersion = state.appConfig?.appVersion ?: "1.0.0",
                    designSystem = designSystem,
                    onDebugClicked = onDebugClicked
                )

                MainBottomSheetContent.ANALYZER -> AnalyzerRenderer(
                    widget = UiWidget.Analyzer(id = "analyzer_main")
                )

                else -> {}
            }
        }
    }
}
