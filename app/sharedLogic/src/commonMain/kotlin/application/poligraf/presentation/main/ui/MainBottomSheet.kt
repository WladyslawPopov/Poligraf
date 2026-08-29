package application.poligraf.presentation.main.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.domain.model.MarkerShape
import application.poligraf.presentation.main.data.MainBottomSheetContent
import application.poligraf.presentation.main.data.MainState
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.components.layout.AppBottomSheet
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSheet(
    modifier: Modifier = Modifier,
    state: MainState,
    defaultSkin: Flow<AnalyzerSkin>,
    markerShape: Flow<MarkerShape>,
    onSkinSelected: (AnalyzerSkin) -> Unit,
    onMarkerShapeSelected: (MarkerShape) -> Unit,
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
                    defaultSkin = defaultSkin,
                    markerShape = markerShape,
                    onSkinSelected = onSkinSelected,
                    onMarkerShapeSelected = onMarkerShapeSelected,
                    onDebugClicked = onDebugClicked
                )

                else -> {}
            }
        }
    }
}
