package application.poligraf.ui.features.analyzer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun InterpretationOverlay(
    interpretation: StringToken?,
    isSynthesized: Boolean,
    synthesisProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        val targetState = when {
            !isSynthesized -> InterpretationState.SYNTHESIZING
            interpretation != null -> InterpretationState.RESULT
            else -> InterpretationState.IDLE
        }

        AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                (fadeIn(tween(800)) + slideInVertically { it / 2 }) togetherWith 
                (fadeOut(tween(600)) + slideOutVertically { -it / 2 })
            },
            label = "interpretation_anim"
        ) { state ->
            when (state) {
                InterpretationState.SYNTHESIZING -> {
                    SynthesizingView(progress = synthesisProgress)
                }
                InterpretationState.RESULT -> {
                    ResultView(interpretation = interpretation)
                }
                InterpretationState.IDLE -> {
                    // Empty space maintained to prevent layout jumps
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun SynthesizingView(progress: Float) {
    val designSystem = LocalDesignSystem.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = designSystem.string(StringToken.ANALYSIS_SYNTHESIZING).uppercase(),
            color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = alpha),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(140.dp).height(2.dp),
            color = designSystem.color(ColorToken.ACCENT_PRIMARY),
            trackColor = designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ResultView(interpretation: StringToken?) {
    val designSystem = LocalDesignSystem.current
    val rawText = interpretation?.let { designSystem.string(it) } ?: ""
    val format = designSystem.string(StringToken.INTERPRETATION_FORMAT)
    val finalTitle = format.replace("%s", rawText)

    Text(
        text = finalTitle,
        color = designSystem.color(ColorToken.TEXT_PRIMARY),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraLight,
            fontStyle = FontStyle.Italic,
            lineHeight = 32.sp
        ),
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

private enum class InterpretationState {
    IDLE, SYNTHESIZING, RESULT
}
