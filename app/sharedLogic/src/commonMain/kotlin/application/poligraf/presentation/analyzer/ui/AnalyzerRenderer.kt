package application.poligraf.presentation.analyzer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import application.poligraf.domain.analyzer.types.AnalyzerMode
import application.poligraf.presentation.analyzer.AnalyzerViewModel
import application.poligraf.ui.features.analyzer.components.AmbientGlow
import application.poligraf.ui.features.analyzer.parts.AnalyzerCoreView
import application.poligraf.ui.features.history.detail.HistoryEditableTitle
import application.poligraf.ui.features.history.detail.HistoryNotesField
import application.poligraf.ui.features.history.detail.SessionNoteItem
import application.poligraf.ui.features.history.detail.SessionSummaryCard
import application.poligraf.ui.utils.KeepScreenOn
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
    val isReviewMode = state.mode == AnalyzerMode.REVIEW

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }

    // Keep screen active (prevent dimming/sleep) during live audio recording
    KeepScreenOn(keepOn = state.isAnalyzing && !state.isPaused)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                if (isReviewMode) {
                    viewModel.toggleTitleEdit(false)
                }
            }
    ) {
        // 0. Ambient Background Glow (Full screen, no padding)
        AmbientGlow(
            signalLevel = state.signalLevel,
            dominantMetric = state.dominantMetric,
            jitterLevel = state.jitterLevel,
            pitchLevel = state.pitchLevel,
            rmsLevel = state.rmsLevel
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + designSystem.dimen(DimenToken.SPACING_XL),
                start = designSystem.dimen(DimenToken.SPACING_LARGE),
                end = designSystem.dimen(DimenToken.SPACING_LARGE)
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_MEDIUM)),
        ) {
            // If Review Mode: Title & Summary Card
            if (isReviewMode) {
                item {
                    HistoryEditableTitle(
                        title = state.session?.title ?: "",
                        isEditing = state.isTitleEditing,
                        onTitleChange = viewModel::onTitleChange,
                        onToggleEdit = { isEditing ->
                            viewModel.toggleTitleEdit(isEditing)
                            if (!isEditing) focusManager.clearFocus()
                        }
                    )
                }

                item {
                    SessionSummaryCard(
                        volatilityStatus = state.volatilityStatus,
                        volatilityColor = state.volatilityColor,
                        fullAnomalyCount = state.fullAnomalyCount,
                        halftoneAnomalyCount = state.halftoneAnomalyCount,
                        noteCount = state.notes.size,
                        durationText = state.durationText,
                        durationMillis = state.currentDurationMillis,
                        conclusionText = state.conclusionText,
                        conclusionColor = state.conclusionColor
                    )
                }
            }

            // 1. Core Analyzer Blocks (Unified Header, Visualizer, Timeline, Controls)
            item {
                AnalyzerCoreView(
                    state = state,
                    onSeek = viewModel::onSeek,
                    showControls = !isReviewMode,
                    onStart = viewModel::onStart,
                    onPauseResume = viewModel::onPauseResume,
                    onSkinChange = viewModel::onSkinChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. Notes Field
            item {
                HistoryNotesField(
                    notes = state.currentNoteText,
                    onNotesChange = viewModel::onNotesChange,
                    onAddNote = {
                        viewModel.onAddNote()
                        focusManager.clearFocus()
                    }
                )
            }

            // 3. Notes List
            items(
                count = state.notes.size,
                key = { index -> state.notes.reversed()[index].id }
            ) { index ->
                val note = state.notes.reversed()[index]
                SessionNoteItem(
                    timestampText = note.timestampText,
                    text = note.text,
                    markerColor = note.markerColor,
                    markerShape = note.markerShape,
                    onDelete = { viewModel.onDeleteNote(note.id) }
                )
            }

            if (isReviewMode) {
                item {
                    Spacer(
                        Modifier
                            .windowInsetsPadding(WindowInsets.ime)
                            .height(designSystem.dimen(DimenToken.SPACING_LARGE))
                    )
                }
            }
        }
    }
}

