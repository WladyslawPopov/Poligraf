package application.poligraf.presentation.main.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.presentation.main.data.MainState
import application.poligraf.ui.components.layout.AppBottomSheet
import application.poligraf.ui.theme.DesignSystem
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
            SettingsContent(
                appVersion = state.appConfig?.appVersion ?: "1.0.0",
                designSystem = designSystem,
                defaultSkin = defaultSkin,
                markerShape = markerShape,
                onSkinSelected = onSkinSelected,
                onMarkerShapeSelected = onMarkerShapeSelected,
                onDebugClicked = onDebugClicked
            )
        }
    }
}
