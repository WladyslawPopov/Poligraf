package application.liedetector.ui.components.state

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
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.*

/**
 * A non-blocking loading indicator overlay.
 */
@Composable
fun LoadingView(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(designSystem.composeColor(ColorToken.SURFACE).copy(alpha = 0.8f))
                    .padding(designSystem.dimen(DimenToken.PADDING_LOADING).dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    color = designSystem.composeColor(ColorToken.ACCENT_ENERGY)
                )
            }
        }
    }
}
