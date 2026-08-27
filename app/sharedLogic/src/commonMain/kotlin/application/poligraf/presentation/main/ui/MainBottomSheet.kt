package application.poligraf.presentation.main.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.presentation.main.data.MainBottomSheetContent
import application.poligraf.presentation.main.data.MainState
import application.poligraf.presentation.main.AnalyzerViewModel
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.components.layout.AppBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSheet(
    modifier: Modifier = Modifier,
    state: MainState,
    analyzerViewModel: AnalyzerViewModel,
    designSystem: DesignSystem,
    onDebugClicked: () -> Unit,
    closeBottomSheet: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            // Only disable closing (moving to Hidden) if recording is active
            if (targetValue == androidx.compose.material3.SheetValue.Hidden) {
                val isCurrentlyRecording = analyzerViewModel.state.value.isRecording && !analyzerViewModel.state.value.isPaused
                !isCurrentlyRecording
            } else {
                true // Always allow opening/expanding
            }
        }
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
                    defaultSkin = analyzerViewModel.preferenceManager.defaultSkin,
                    markerShape = analyzerViewModel.preferenceManager.markerShape,
                    onSkinSelected = { analyzerViewModel.preferenceManager.setDefaultSkin(it) },
                    onMarkerShapeSelected = { analyzerViewModel.preferenceManager.setMarkerShape(it) },
                    onDebugClicked = onDebugClicked
                )

                MainBottomSheetContent.ANALYZER -> AnalyzerRenderer(
                    viewModel = analyzerViewModel,
                    onClose = closeBottomSheet
                )

                else -> {}
            }
        }
    }
}
