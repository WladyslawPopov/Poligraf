package application.poligraf.presentation.analyzer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.analyzer.AnalyzerViewModel
import application.poligraf.ui.features.recorder.AmbientGlow
import application.poligraf.ui.features.recorder.AnalyzerControls
import application.poligraf.ui.features.recorder.AnomalyTimeline
import application.poligraf.ui.features.recorder.InterpretationOverlay
import application.poligraf.ui.features.recorder.MetricRow
import application.poligraf.ui.features.recorder.SkinSwitcher
import application.poligraf.ui.features.recorder.VisualizationContent
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken

@Composable
fun AnalyzerRenderer(
    viewModel: AnalyzerViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val designSystem = LocalDesignSystem.current
    val state by viewModel.state.collectAsState()

    val displayFrame = state.displayFrame
    val displayJitter = displayFrame?.jitter ?: 0f
    val displayPitch = displayFrame?.pitch ?: 0f
    val displayRms = displayFrame?.rms ?: 0f

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        // 0. Ambient Background Glow
        AmbientGlow(
            isAnomalous = state.isDisplayAnomalous,
            jitter = displayJitter,
            pitch = displayPitch
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = designSystem.dimen(DimenToken.SPACING_LARGE)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))

            // 1. Global Status Overlay
            InterpretationOverlay(interpretation = state.activeInterpretation)

            // 2. Main Visualization Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                VisualizationContent(
                    skin = state.currentSkin,
                    jitter = state.jitterLevel,
                    pitch = state.pitchLevel,
                    rms = state.rmsLevel,
                    isPaused = state.isPaused
                )

                SkinSwitcher(
                    currentSkin = state.currentSkin,
                    onSkinChange = viewModel::onSkinChange,
                    showLabel = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                )
            }

            // 3. Metrics
            MetricRow(
                jitter = displayJitter,
                pitch = displayPitch,
                rms = displayRms
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            // 4. Anomaly Timeline
            AnomalyTimeline(
                markers = state.timelineMarkers,
                currentDurationMillis = state.currentDurationMillis,
                seekPositionMillis = state.seekPositionMillis,
                isPaused = state.isPaused,
                onSeek = viewModel::onSeek
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            // 5. Controls
            AnalyzerControls(
                isRecording = state.isRecording,
                isPaused = state.isPaused,
                onStart = viewModel::onStart,
                onPauseResume = viewModel::onPauseResume,
            )

            Spacer(
                Modifier.height(
                    contentPadding.calculateBottomPadding() + designSystem.dimen(
                        DimenToken.SPACING_LARGE
                    )
                )
            )
        }
    }
}
