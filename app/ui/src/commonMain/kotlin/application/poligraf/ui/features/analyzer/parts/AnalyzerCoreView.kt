package application.poligraf.ui.features.analyzer.parts

import application.poligraf.ui.features.analyzer.components.AnomalyTimeline
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.ui.components.containers.AppCard
import application.poligraf.ui.features.analyzer.components.InterpretationOverlay
import application.poligraf.ui.features.analyzer.components.MetricLegend
import application.poligraf.ui.features.analyzer.components.MetricRow
import application.poligraf.ui.features.analyzer.components.SkinSwitcher
import application.poligraf.ui.features.analyzer.visualizations.VisualizationContent
import application.poligraf.ui.foundation.state.AnalyzerState
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken

@Composable
fun AnalyzerCoreView(
    state: AnalyzerState,
    onSeek: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    showControls: Boolean = false,
    onStart: () -> Unit = {},
    onPauseResume: () -> Unit = {},
    onSkinChange: (AnalyzerSkin) -> Unit = {},
    showHeader: Boolean = true,
) {
    val designSystem = LocalDesignSystem.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_MEDIUM))
    ) {
        // 0. Consolidated Header (Synthesis Status + Skin Switcher)
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InterpretationOverlay(
                    interpretation = state.activeInterpretation,
                    isSynthesized = state.isCalibrated,
                    synthesisProgress = state.calibrationProgress,
                    modifier = Modifier.weight(1f)
                )

                SkinSwitcher(
                    currentSkin = state.currentSkin,
                    onSkinChange = onSkinChange,
                    showLabel = true
                )
            }
        }

        // 1. Main Visualization & Metrics Card
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(designSystem.dimen(DimenToken.SPACING_MEDIUM)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_MEDIUM))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VisualizationContent(
                        skin = state.currentSkin,
                        jitter = state.jitterLevel,
                        pitch = state.pitchLevel,
                        rms = state.rmsLevel,
                        isPaused = state.isPaused
                    )
                }

                if (state.currentSkin != AnalyzerSkin.STATE_MAP) {
                    MetricLegend()
                }

                MetricRow(
                    jitterLevel = state.jitterLevel,
                    pitchLevel = state.pitchLevel,
                    rmsLevel = state.rmsLevel
                )
            }
        }

        // 2. Anomaly Timeline Card
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = designSystem.dimen(DimenToken.SPACING_SMALL))
        ) {
            AnomalyTimeline(
                markers = state.timelineMarkers,
                notes = state.notes,
                currentDurationMillis = state.currentDurationMillis,
                seekPositionMillis = state.seekPositionMillis,
                isPaused = state.isPaused,
                onSeek = onSeek
            )
        }

        if (showControls) {
            AnalyzerControls(
                isAnalyzing = state.isAnalyzing,
                isPaused = state.isPaused,
                onStart = onStart,
                onPauseResume = onPauseResume,
            )
        }
    }
}
