package application.poligraf.ui.features.render

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.foundation.models.UiWidget
import application.poligraf.ui.components.text.TypingText

@Composable
fun WelcomeTextRenderer(
    widget: UiWidget.WelcomeText
) {
    val designSystem = LocalDesignSystem.current
    val fullText = designSystem.string(widget.textToken) + (widget.emoji?.let { "\u00A0$it" } ?: "")

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
            color = designSystem.color(widget.colorToken),
            textAlign = TextAlign.Center,
            typingDelay = widget.typingDelay
        )
    }
}


