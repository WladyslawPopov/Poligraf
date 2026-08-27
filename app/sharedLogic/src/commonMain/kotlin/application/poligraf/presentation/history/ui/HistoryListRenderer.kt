package application.poligraf.presentation.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.history.data.HistoryState
import application.poligraf.presentation.history.data.SessionUiModel
import application.poligraf.ui.features.history.HistoryItem
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun HistoryListRenderer(
    state: HistoryState,
    onSessionClick: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
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
                HistoryItem(
                    title = session.title,
                    dateText = session.dateText,
                    markerCount = session.markerCount,
                    onClick = { onSessionClick(session.id) }
                )
            }
        }
    }
}
