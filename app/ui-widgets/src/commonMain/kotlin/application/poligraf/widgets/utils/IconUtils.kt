package application.poligraf.widgets.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import application.poligraf.uicore.theme.IconResource

@Composable
expect fun AppIcon(
    icon: IconResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
)
