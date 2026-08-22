package application.poligraf.widgets.state

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken

/**
 * A non-blocking loading indicator overlay.
 */
@Composable
fun LoadingView(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Box(modifier = modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = designSystem.dimen(
                DimenToken.SPACING_MEDIUM))
        ) {
            Box(
                modifier = Modifier.size(designSystem.dimen(DimenToken.LOADING_INDICATOR_SIZE))
                    .clip(CircleShape)
                    .background(designSystem.color(ColorToken.SURFACE).copy(alpha = 0.8f))
                    .padding(designSystem.dimen(DimenToken.PADDING_LOADING)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = designSystem.dimen(DimenToken.LOADING_INDICATOR_STROKE),
                    color = designSystem.color(ColorToken.ACCENT_ENERGY)
                )
            }
        }
    }
}
