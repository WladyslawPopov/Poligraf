package application.poligraf.widgets.utils

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import application.poligraf.uicore.theme.IconResource

@Composable
actual fun AppIcon(
    icon: IconResource,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
