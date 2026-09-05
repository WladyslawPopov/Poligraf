package application.poligraf.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Custom vector icons and shapes for the Poligraf Design System theme.
 */
object AppIcons {
    val GeometricDiamond: ImageVector by lazy {
        ImageVector.Builder(
            name = "GeometricDiamond",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = PathParser().parsePathString("M12,2 L22,12 L12,22 L2,12 Z").toNodes(),
            fill = SolidColor(Color.White)
        ).build()
    }
}
