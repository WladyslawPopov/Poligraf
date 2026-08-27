package application.poligraf.presentation.history.ui

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.components.buttons.AppIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryToolbar(
    toolbar: AppToolbar,
    designSystem: DesignSystem,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = designSystem.string(toolbar.titleToken),
                color = designSystem.color(toolbar.contentColor),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            AppIconButton(
                icon = IconToken.ARROW_BACK,
                onClick = onBack
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
