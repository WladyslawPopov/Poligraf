package application.poligraf.presentation.analyzer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import application.poligraf.presentation.analyzer.AnalyzerViewModel
import application.poligraf.ui.features.analyzer.components.AmbientGlow
import application.poligraf.ui.features.analyzer.components.InterpretationOverlay
import application.poligraf.ui.features.analyzer.components.SkinSwitcher
import application.poligraf.ui.features.analyzer.parts.AnalyzerCoreView
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
        // 0. Ambient Background Glow (Full screen, no padding)
        AmbientGlow(
            isAnomalous = state.isDisplayAnomalous,
            jitter = state.displayFrame?.jitterScore ?: 0f,
            pitch = state.displayFrame?.pitchScore ?: 0f
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
            // 1. Consolidated Header (Synthesis Status + Skin Switcher)
            item {
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
                        onSkinChange = viewModel::onSkinChange,
                        showLabel = true
                    )
                }
            }

            // 2. Core Analyzer Blocks
            item {
                AnalyzerCoreView(
                    state = state,
                    onSeek = viewModel::onSeek,
                    showControls = true,
                    onStart = viewModel::onStart,
                    onPauseResume = viewModel::onPauseResume
                )
            }

            // 3. Notes Field
            item {
                application.poligraf.ui.features.history.detail.HistoryNotesField(
                    notes = state.currentNoteText,
                    onNotesChange = viewModel::onNotesChange,
                    onAddNote = {
                        viewModel.onAddNote()
                        focusManager.clearFocus()
                    }
                )
            }

            // 4. Notes List
            items(
                count = state.notes.size,
                key = { index -> state.notes.reversed()[index].id }
            ) { index ->
                val note = state.notes.reversed()[index]
                application.poligraf.ui.features.history.detail.SessionNoteItem(
                    timestampText = note.timestampText,
                    text = note.text,
                    markerColor = note.markerColor,
                    markerShape = note.markerShape,
                    onDelete = { viewModel.onDeleteNote(note.id) }
                )
            }
        }
    }
}
