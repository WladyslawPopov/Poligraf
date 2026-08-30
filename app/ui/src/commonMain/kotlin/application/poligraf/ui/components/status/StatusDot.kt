package application.poligraf.ui.components.status

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun StatusDot(
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    pulse: Boolean = true
) {
    val designSystem = LocalDesignSystem.current
    
    val color = if (isAnalyzing) {
        designSystem.color(ColorToken.STATE_SUCCESS)
    } else {
        designSystem.color(ColorToken.STATE_ERROR)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by if (pulse && isAnalyzing) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        rememberInfiniteTransition().animateFloat(1f, 1f, infiniteRepeatable(tween(1000)))
    }

    val scale by if (pulse && isAnalyzing) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        rememberInfiniteTransition().animateFloat(1f, 1f, infiniteRepeatable(tween(1000)))
    }

    Box(modifier = modifier.size(size * 2), contentAlignment = androidx.compose.ui.Alignment.Center) {
        if (pulse && isAnalyzing) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f))
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
    }
}
