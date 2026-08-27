package application.poligraf.presentation.history_detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.history_detail.HistoryDetailViewModel
import application.poligraf.ui.features.history.HistoryEditableTitle
import application.poligraf.ui.features.history.HistoryNotesField
import application.poligraf.ui.features.history.SessionSummaryCard
import application.poligraf.ui.features.recorder.AnomalyTimeline
import application.poligraf.ui.features.recorder.EqualizerVisualization
import application.poligraf.ui.features.recorder.MetricItem
import application.poligraf.ui.features.recorder.RingsVisualization
import application.poligraf.ui.features.recorder.StateMapVisualization
import application.poligraf.ui.features.recorder.VoiceRibbonVisualization
import application.poligraf.engine.models.AnalyzerSkin
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun HistoryDetailRenderer(
    viewModel: HistoryDetailViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val designSystem = LocalDesignSystem.current
    val state by viewModel.state.collectAsState()
    val analyzerState = state.analyzerState

    val displayJitter = analyzerState.displayFrame?.jitter ?: 0f
    val displayPitch = analyzerState.displayFrame?.pitch ?: 0f
    val displayRms = analyzerState.displayFrame?.rms ?: 0f

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(designSystem.color(ColorToken.SURFACE_BACKGROUND))
            .padding(contentPadding)
            .verticalScroll(scrollState)
            .padding(designSystem.dimen(DimenToken.SPACING_LARGE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Editable Title
        HistoryEditableTitle(
            title = state.session?.title ?: "",
            onTitleChange = viewModel::onTitleChange
        )

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

        // 2. Summary Card
        SessionSummaryCard(
            volatilityStatus = state.volatilityStatus,
            volatilityColor = state.volatilityColor,
            anomalyCount = state.anomalyCount,
            durationText = state.durationText,
            conclusionText = state.conclusionText,
            conclusionColor = state.conclusionColor
        )

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

        // 3. Visualization Area (Fixed Height for stability)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            when (analyzerState.currentSkin) {
                AnalyzerSkin.STATE_MAP -> StateMapVisualization(
                    jitterLevel = analyzerState.jitterLevel,
                    pitchLevel = analyzerState.pitchLevel,
                    rmsLevel = analyzerState.rmsLevel
                )

                AnalyzerSkin.VOICE_RIBBON -> VoiceRibbonVisualization(
                    jitterLevel = analyzerState.jitterLevel,
                    pitchLevel = analyzerState.pitchLevel,
                    rmsLevel = analyzerState.rmsLevel,
                    isPaused = true
                )

                AnalyzerSkin.EQUALIZER -> EqualizerVisualization(
                    jitterLevel = analyzerState.jitterLevel,
                    pitchLevel = analyzerState.pitchLevel,
                    rmsLevel = analyzerState.rmsLevel
                )

                AnalyzerSkin.RINGS -> RingsVisualization(
                    jitterLevel = analyzerState.jitterLevel,
                    pitchLevel = analyzerState.pitchLevel,
                    rmsLevel = analyzerState.rmsLevel
                )
            }
        }

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

        // 4. Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = designSystem.string(StringToken.METRIC_JITTER),
                value = if (displayJitter > 0f) "${displayJitter.toInt()}%" else "0%",
                color = designSystem.color(ColorToken.CHART_JITTER)
            )
            MetricItem(
                label = designSystem.string(StringToken.METRIC_PITCH),
                value = if (displayPitch > 50f) displayPitch.toInt().toString() else "0",
                color = designSystem.color(ColorToken.CHART_PITCH)
            )
            MetricItem(
                label = designSystem.string(StringToken.METRIC_RMS),
                value = (displayRms * 100).toInt().coerceIn(0, 100).toString(),
                color = designSystem.color(ColorToken.CHART_RMS)
            )
        }

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

        // 5. Anomaly Timeline
        AnomalyTimeline(
            markers = analyzerState.timelineMarkers,
            currentDurationMillis = analyzerState.currentDurationMillis,
            seekPositionMillis = analyzerState.seekPositionMillis,
            isPaused = true,
            onSeek = viewModel::onSeek
        )

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

        // 6. Notes Field
        HistoryNotesField(
            notes = state.session?.notes ?: "",
            onNotesChange = viewModel::onNotesChange,
            onSave = viewModel::onSaveMetadata,
            isSaving = state.isSaving
        )

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

        // Skin Switcher (Simplified for history)
        val currentSkinName = when (analyzerState.currentSkin) {
            AnalyzerSkin.STATE_MAP -> designSystem.string(StringToken.SKIN_STATE_MAP)
            AnalyzerSkin.VOICE_RIBBON -> designSystem.string(StringToken.SKIN_VOICE_RIBBON)
            AnalyzerSkin.EQUALIZER -> designSystem.string(StringToken.SKIN_EQUALIZER)
            AnalyzerSkin.RINGS -> designSystem.string(StringToken.SKIN_RINGS)
        }

        TextButton(onClick = {
            val entries = AnalyzerSkin.entries
            val nextIndex = (analyzerState.currentSkin.ordinal + 1) % entries.size
            viewModel.onSkinChange(entries[nextIndex])
        }) {
            Text(
                text = currentSkinName,
                color = designSystem.color(ColorToken.ACCENT_PRIMARY)
            )
        }
    }
}
