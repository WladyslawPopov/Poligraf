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
import application.poligraf.ui.features.history.detail.HistoryEditableTitle
import application.poligraf.ui.features.history.detail.HistoryNotesField
import application.poligraf.ui.features.history.detail.SessionNoteItem
import application.poligraf.ui.features.history.detail.SessionSummaryCard
import application.poligraf.ui.features.analyzer.components.AmbientGlow
import application.poligraf.ui.features.analyzer.parts.AnalyzerCoreView
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
            jitter = analyzerState.displayFrame?.jitterScore ?: 0f,
            pitch = analyzerState.displayFrame?.pitchScore ?: 0f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + designSystem.dimen(DimenToken.SPACING_XL),
                    start = designSystem.dimen(DimenToken.SPACING_LARGE),
                    end = designSystem.dimen(DimenToken.SPACING_LARGE)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            // Core Analyzer (Unified component)
            AnalyzerCoreView(
                state = analyzerState,
                onSeek = viewModel::onSeek,
                showControls = false,
                modifier = Modifier.fillMaxWidth()
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
