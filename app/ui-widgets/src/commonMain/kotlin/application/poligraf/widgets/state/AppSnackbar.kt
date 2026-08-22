package application.poligraf.widgets.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.uicore.types.ToastType
import application.poligraf.widgets.AppIcon

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
        ToastType.SUCCESS -> designSystem.color(ColorToken.TRUTH)
        ToastType.ERROR -> designSystem.color(ColorToken.STRESS)
        ToastType.WARNING -> designSystem.color(ColorToken.ACCENT_PRIMARY)
    }

    Snackbar(
        modifier = Modifier.padding(12.dp),
        containerColor = designSystem.color(ColorToken.GLASS_BASE),
        contentColor = designSystem.color(ColorToken.TEXT_PRIMARY),
        actionContentColor = accentColor,
        dismissActionContentColor = designSystem.color(ColorToken.TEXT_SECONDARY),
        shape = RoundedCornerShape(designSystem.dimen(DimenToken.CORNER_RADIUS)),
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
                    AppIcon(
                        icon = designSystem.icon(IconToken.CLOSE),
                        contentDescription = null,
                        tint = designSystem.color(ColorToken.TEXT_SECONDARY)
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
