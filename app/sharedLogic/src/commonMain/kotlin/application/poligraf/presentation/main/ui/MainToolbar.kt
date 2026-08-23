package application.poligraf.presentation.main.ui

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.components.buttons.AppIconButton
import application.poligraf.ui.components.text.TypingText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    toolbar: AppToolbar,
    designSystem: DesignSystem,
    onWidgetAction: (WidgetAction) -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            TypingText(
                fullText = designSystem.string(toolbar.titleToken),
                color = designSystem.color(toolbar.contentColor),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            toolbar.settingsAction?.let { action ->
                AppIconButton(
                    icon = IconToken.SETTINGS,
                    onClick = { onWidgetAction(action) }
                )
            }
        },
        actions = {
            toolbar.historyAction?.let { action ->
                AppIconButton(
                    icon = IconToken.HISTORY,
                    onClick = { onWidgetAction(action) }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
