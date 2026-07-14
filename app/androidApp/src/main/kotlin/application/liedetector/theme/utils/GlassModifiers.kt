package application.liedetector.theme.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DesignSystem

/**
 * Provides a glass-like styling (tint + border). 
 * Content remains sharp.
 */
fun Modifier.glassPanel(
    designSystem: DesignSystem,
    cornerRadius: Dp = 20.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(designSystem.composeColor(ColorToken.GLASS_BASE))
    .border(
        width = 0.5.dp,
        color = designSystem.composeColor(ColorToken.GLASS_BORDER),
        shape = RoundedCornerShape(cornerRadius)
    )
