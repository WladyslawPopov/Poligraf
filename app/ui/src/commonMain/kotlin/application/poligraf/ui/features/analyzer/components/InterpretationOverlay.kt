package application.poligraf.ui.features.analyzer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Multi-tier status cascade widget with normalized eye-legible opacities.
 * Primary center headline text maintains 85%-100% opacity for crisp readability,
 * while secondary floating texts maintain 45%-75% opacity for clear background context.
 */
@Composable
fun InterpretationOverlay(
    interpretation: StringToken?,
    modifier: Modifier = Modifier,
    primaryAlpha: Float = 1.0f,
    secondaryInterpretations: List<StringToken> = emptyList(),
    secondaryInterpretationsWithAlpha: List<Pair<StringToken, Float>> = emptyList(),
) {
    val designSystem = LocalDesignSystem.current
    val topSecondaryPair = secondaryInterpretationsWithAlpha.getOrNull(0)
    val bottomSecondaryPair = secondaryInterpretationsWithAlpha.getOrNull(1)

    val topSecondaryToken = topSecondaryPair?.first ?: secondaryInterpretations.getOrNull(0)
    val bottomSecondaryToken = bottomSecondaryPair?.first ?: secondaryInterpretations.getOrNull(1)

    val effectivePrimaryAlpha = (primaryAlpha * 0.25f + 0.75f).coerceIn(0.85f, 1.0f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = topSecondaryToken,
            transitionSpec = {
                fadeIn(tween(700, easing = LinearOutSlowInEasing)) togetherWith
                        fadeOut(tween(700, easing = FastOutLinearInEasing))
            },
            label = "top_secondary_anim"
        ) { token ->
            if (token != null) {
                Text(
                    text = designSystem.string(token),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    ),
                    color = designSystem.color(ColorToken.TEXT_SECONDARY),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        AnimatedContent(
            targetState = interpretation ?: StringToken.STATUS_CALM,
            transitionSpec = {
                (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }) togetherWith
                        (fadeOut(tween(400)) + slideOutVertically(tween(400)) { -it / 2 })
            },
            label = "interpretation_text_anim"
        ) { token ->
            val rawText = designSystem.string(token)

            val baseColor = when (token) {
                StringToken.STATUS_WARMUP -> designSystem.color(ColorToken.TEXT_SECONDARY)
                    .copy(alpha = 0.7f)

                StringToken.STATUS_CLIPPING -> designSystem.color(ColorToken.STATE_ERROR)
                StringToken.STATUS_LOW_SNR -> designSystem.color(ColorToken.STATE_WARNING)
                StringToken.STATUS_CALM -> designSystem.color(ColorToken.TEXT_PRIMARY)
                StringToken.STATUS_MILD_FLUCTUATION -> designSystem.color(ColorToken.TEXT_PRIMARY)
                else -> designSystem.color(ColorToken.TEXT_PRIMARY)
            }

            val textColor = baseColor.copy(alpha = effectivePrimaryAlpha)

            TypingText(
                fullText = rawText,
                color = textColor,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 30.sp
                ),
                textAlign = TextAlign.Center,
                typingDelay = 25L
            )
        }

        AnimatedContent(
            targetState = bottomSecondaryToken,
            transitionSpec = {
                fadeIn(tween(700, easing = LinearOutSlowInEasing)) togetherWith
                        fadeOut(tween(700, easing = FastOutLinearInEasing))
            },
            label = "bottom_secondary_anim"
        ) { token ->
            if (token != null) {
                Text(
                    text = designSystem.string(token),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    ),
                    color = designSystem.color(ColorToken.TEXT_SECONDARY),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
