package application.poligraf.presentation.history.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.ui.components.containers.AppCard
import application.poligraf.ui.features.history.list.HistoryItem
import application.poligraf.ui.features.history.state.HistoryState
import application.poligraf.ui.features.history.state.SessionUiModel
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun HistoryListRenderer(
    state: HistoryState,
    onSessionClick: (String) -> Unit,
    onSessionLongClick: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val designSystem = LocalDesignSystem.current

    if (state.sessions.isEmpty() && !state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = designSystem.string(StringToken.HISTORY_EMPTY),
                color = designSystem.color(ColorToken.TEXT_SECONDARY),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_MEDIUM))
        ) {
            items(state.sessions) { session ->
                HistoryListItemWrapper(
                    session = session,
                    isSelected = state.selectedIds.contains(session.id),
                    isSelectionMode = state.isSelectionMode,
                    onClick = { onSessionClick(session.id) },
                    onLongClick = { onSessionLongClick(session.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryListItemWrapper(
    session: SessionUiModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val designSystem = LocalDesignSystem.current

    val backgroundColor = when {
        isSelected -> designSystem.color(ColorToken.STATE_ERROR).copy(alpha = 0.15f)
        else -> designSystem.color(ColorToken.SURFACE_PRIMARY).copy(alpha = 0.8f)
    }

    val borderColor = when {
        isSelected -> designSystem.color(ColorToken.STATE_ERROR)
        isSelectionMode -> designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    AppCard(
        modifier = Modifier
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        contentPadding = PaddingValues(designSystem.dimen(DimenToken.SPACING_MEDIUM))
    ) {
        HistoryItem(
            title = session.title,
            dateText = session.dateText,
            fullAnomalyCount = session.fullAnomalyCount,
            halftoneAnomalyCount = session.halftoneAnomalyCount,
            noteCount = session.noteCount
        )
    }
}
