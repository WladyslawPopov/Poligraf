package application.poligraf.ui.features.recorder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun InterpretationOverlay(
    interpretation: StringToken?,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = interpretation?.let { "interp" } ?: "none",
            transitionSpec = {
                fadeIn(tween(600)) togetherWith fadeOut(tween(600))
            }, label = "status"
        ) { target ->
            when (target) {
                "interp" -> {
                    val rawText = interpretation?.let { designSystem.string(it) } ?: ""
                    val format = designSystem.string(StringToken.INTERPRETATION_FORMAT)
                    val finalTitle = format.replace("%s", rawText)

                    Text(
                        text = finalTitle,
                        color = designSystem.color(ColorToken.TEXT_PRIMARY),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                else -> {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}
