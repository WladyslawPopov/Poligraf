package application.poligraf.widgets.utils

import androidx.compose.material3.Text
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
    // SF Symbols or similar implementation for iOS
    Text(text = "Icon", modifier = modifier, color = tint)
}
