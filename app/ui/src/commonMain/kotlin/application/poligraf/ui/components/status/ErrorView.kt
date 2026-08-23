package application.poligraf.ui.components.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.engine.error.ErrorType
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

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

    val title = designSystem.string(StringToken.ERROR_TITLE)
    val msg = designSystem.string(StringToken.ERROR_MESSAGE)

    val emoji = when (type) {
        ErrorType.NO_INTERNET -> "🌐"
        ErrorType.SERVER_UNAVAILABLE -> "📡"
        else -> "⚠️"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(designSystem.dimen(DimenToken.SPACING_XL)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    designSystem.color(ColorToken.GLASS_BASE).copy(alpha = 0.3f),
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
                color = designSystem.color(ColorToken.TEXT_PRIMARY),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_SMALL)))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyLarge,
                color = designSystem.color(ColorToken.TEXT_SECONDARY),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = designSystem.color(ColorToken.ACCENT_ENERGY),
                    contentColor = designSystem.color(ColorToken.TEXT_INVERTED)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(designSystem.string(StringToken.RETRY))
            }
        }
    }
}
