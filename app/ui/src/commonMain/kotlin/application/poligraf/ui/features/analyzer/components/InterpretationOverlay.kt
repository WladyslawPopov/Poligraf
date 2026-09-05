package application.poligraf.ui.features.analyzer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.poligraf.ui.components.text.TypingText
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Unified continuous text headline widget featuring an upward floating and dissolving
 * shadow transition combined with character-by-character typing animation.
 */
@Composable
fun InterpretationOverlay(
    interpretation: StringToken?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = interpretation ?: StringToken.STATUS_CALM,
            transitionSpec = {
                (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }) togetherWith
                        (fadeOut(tween(400)) + slideOutVertically(tween(400)) { -it / 2 })
            },
            label = "interpretation_text_anim"
        ) { token ->
            val designSystem = LocalDesignSystem.current
            val rawText = designSystem.string(token)

            val textColor = when (token) {
                StringToken.STATUS_WARMUP -> designSystem.color(ColorToken.TEXT_SECONDARY)
                    .copy(alpha = 0.7f)

                StringToken.STATUS_CLIPPING -> designSystem.color(ColorToken.STATE_ERROR)
                StringToken.STATUS_LOW_SNR -> designSystem.color(ColorToken.STATE_WARNING)
                StringToken.STATUS_CALM -> designSystem.color(ColorToken.TEXT_PRIMARY)
                StringToken.STATUS_MILD_FLUCTUATION -> designSystem.color(ColorToken.TEXT_PRIMARY)
                else -> designSystem.color(ColorToken.TEXT_PRIMARY)
            }

            TypingText(
                fullText = rawText,
                color = textColor,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 32.sp
                ),
                textAlign = TextAlign.Center,
                typingDelay = 25L,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
