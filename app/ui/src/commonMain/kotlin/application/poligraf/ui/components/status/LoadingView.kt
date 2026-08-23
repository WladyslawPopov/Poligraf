package application.poligraf.ui.components.status

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
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken

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
                modifier = Modifier.size(designSystem.dimen(DimenToken.SPACING_XXL))
                    .clip(CircleShape)
                    .background(designSystem.color(ColorToken.SURFACE_PRIMARY).copy(alpha = 0.8f))
                    .padding(designSystem.dimen(DimenToken.SPACING_SMALL)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = designSystem.color(ColorToken.ACCENT_ENERGY)
                )
            }
        }
    }
}
