package application.poligraf.ui.features.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import application.poligraf.ui.components.text.TypingText
import application.poligraf.ui.features.main.models.MainWelcomeModel
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken

@Composable
fun MainWelcomeText(
    model: MainWelcomeModel,
) {
    val designSystem = LocalDesignSystem.current
    val fullText = designSystem.string(model.textToken) + (model.emoji?.let { "\u00A0$it" } ?: "")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = designSystem.dimen(DimenToken.SPACING_XL))
            .padding(designSystem.dimen(DimenToken.SPACING_LARGE)),
        contentAlignment = Alignment.Center
    ) {
        TypingText(
            fullText = fullText,
            style = MaterialTheme.typography.displaySmall,
            color = designSystem.color(model.colorToken),
            textAlign = TextAlign.Center,
            typingDelay = model.typingDelay
        )
    }
}
