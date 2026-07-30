package application.liedetector.ui.components.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.*
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.uicore.types.ToastType

/**
 * A custom Snackbar that follows the Design System's glass and neon aesthetic.
 */
@Composable
fun AppSnackBar(
    data: SnackbarData,
    type: ToastType
) {
    val designSystem = LocalDesignSystem.current
    
    val accentColor = when (type) {
        ToastType.SUCCESS -> designSystem.composeColor(ColorToken.TRUTH)
        ToastType.ERROR -> designSystem.composeColor(ColorToken.STRESS)
        ToastType.WARNING -> designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
    }

    Snackbar(
        modifier = Modifier.padding(12.dp),
        containerColor = designSystem.composeColor(ColorToken.GLASS_BASE),
        contentColor = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
        actionContentColor = accentColor,
        dismissActionContentColor = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
        shape = RoundedCornerShape(designSystem.dimen(DimenToken.CORNER_RADIUS).dp),
        action = if (data.visuals.actionLabel != null) {
            @Composable {
                TextButton(onClick = { data.performAction() }) {
                    Text(data.visuals.actionLabel!!, color = accentColor)
                }
            }
        } else null,
        dismissAction = if (data.visuals.withDismissAction) {
            @Composable {
                IconButton(onClick = { data.dismiss() }) {
                    Icon(
                        imageVector = designSystem.icon(IconToken.CLOSE),
                        contentDescription = null,
                        tint = designSystem.composeColor(ColorToken.TEXT_SECONDARY)
                    )
                }
            }
        } else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Semantic Neon Indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(data.visuals.message)
        }
    }
}
