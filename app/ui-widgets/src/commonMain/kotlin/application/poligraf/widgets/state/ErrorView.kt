package application.poligraf.widgets.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.engine.error.ErrorType
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.widgets.utils.composeColor
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Renders an error state as a full-page overlay within the screen's content area.
 */
@Composable
fun ErrorView(
    type: ErrorType,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    val title = when (type) {
        ErrorType.NO_INTERNET -> designSystem.strings.errors.noInternetTitle
        ErrorType.SERVER_UNAVAILABLE -> designSystem.strings.errors.serverTitle
        else -> designSystem.strings.errors.unknownTitle
    }

    val msg = when (type) {
        ErrorType.NO_INTERNET -> designSystem.strings.errors.noInternetMsg
        ErrorType.SERVER_UNAVAILABLE -> designSystem.strings.errors.serverMsg
        else -> designSystem.strings.errors.unknownMsg
    }

    val emoji = when (type) {
        ErrorType.NO_INTERNET -> "🌐"
        ErrorType.SERVER_UNAVAILABLE -> "📡"
        else -> "⚠️"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(designSystem.dimen(DimenToken.PADDING_ERROR).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    designSystem.composeColor(ColorToken.GLASS_BASE).let { it as ComposeColor }.copy(alpha = 0.3f),
                    MaterialTheme.shapes.extraLarge
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_SMALL).dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyLarge,
                color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = designSystem.composeColor(ColorToken.ACCENT_ENERGY),
                    contentColor = designSystem.composeColor(ColorToken.TEXT_INVERTED)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(designSystem.strings.errors.retry)
            }
        }
    }
}
