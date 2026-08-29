package application.poligraf.presentation.history_detail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.history_detail.HistoryDetailViewModel
import application.poligraf.ui.features.history.HistoryEditableTitle
import application.poligraf.ui.features.history.HistoryNotesField
import application.poligraf.ui.features.history.SessionNoteItem
import application.poligraf.ui.features.history.SessionSummaryCard
import application.poligraf.ui.features.recorder.AmbientGlow
import application.poligraf.ui.features.recorder.AnomalyTimeline
import application.poligraf.ui.features.recorder.MetricRow
import application.poligraf.ui.features.recorder.SkinSwitcher
import application.poligraf.ui.features.recorder.VisualizationContent
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken

@Composable
fun HistoryDetailRenderer(
    viewModel: HistoryDetailViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val designSystem = LocalDesignSystem.current
    val state by viewModel.state.collectAsState()
    val analyzerState = state.analyzerState

    val displayFrame = analyzerState.displayFrame
    val displayJitter = displayFrame?.jitter ?: 0f
    val displayPitch = displayFrame?.pitch ?: 0f
    val displayRms = displayFrame?.rms ?: 0f

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                viewModel.toggleTitleEdit(false)
            }
    ) {
        AmbientGlow(
            isAnomalous = analyzerState.isDisplayAnomalous,
            jitter = displayJitter,
            pitch = displayPitch
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = designSystem.dimen(DimenToken.SPACING_LARGE)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))

            HistoryEditableTitle(
                title = state.session?.title ?: "",
                isEditing = state.isTitleEditing,
                onTitleChange = viewModel::onTitleChange,
                onToggleEdit = { isEditing ->
                    viewModel.toggleTitleEdit(isEditing)
                    if (!isEditing) focusManager.clearFocus()
                }
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

            SessionSummaryCard(
                volatilityStatus = state.volatilityStatus,
                volatilityColor = state.volatilityColor,
                anomalyCount = state.anomalyCount,
                durationText = state.durationText,
                conclusionText = state.conclusionText,
                conclusionColor = state.conclusionColor
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                VisualizationContent(
                    skin = analyzerState.currentSkin,
                    jitter = analyzerState.jitterLevel,
                    pitch = analyzerState.pitchLevel,
                    rms = analyzerState.rmsLevel,
                    isPaused = true
                )

                SkinSwitcher(
                    currentSkin = analyzerState.currentSkin,
                    onSkinChange = viewModel::onSkinChange,
                    showLabel = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                )
            }

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

            MetricRow(
                jitter = displayJitter,
                pitch = displayPitch,
                rms = displayRms
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            AnomalyTimeline(
                markers = analyzerState.timelineMarkers,
                currentDurationMillis = analyzerState.currentDurationMillis,
                seekPositionMillis = analyzerState.seekPositionMillis,
                isPaused = true,
                onSeek = viewModel::onSeek
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            HistoryNotesField(
                notes = state.currentNoteText,
                onNotesChange = viewModel::onNotesChange,
                onAddNote = {
                    viewModel.onAddNote()
                    focusManager.clearFocus()
                }
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

            // Notes List
            state.notes.reversed().forEach { note ->
                SessionNoteItem(
                    timestampText = note.timestampText,
                    text = note.text,
                    markerColor = note.markerColor,
                    markerShape = note.markerShape,
                    onDelete = { viewModel.onDeleteNote(note.id) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(
                Modifier
                    .windowInsetsPadding(WindowInsets.ime)
                    .height(designSystem.dimen(DimenToken.SPACING_LARGE))
            )
        }
    }
}
