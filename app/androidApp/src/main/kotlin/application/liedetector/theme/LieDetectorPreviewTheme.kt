package application.liedetector.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import application.liedetector.uicore.theme.*

/**
 * A mock resource provider for IDE Previews.
 */
class FakeResourceProvider : ResourceProvider {
    override fun getString(key: String): String = key
    override fun getColorHex(token: ColorToken): String = "#808080"
    override fun getDimension(token: DimenToken): Float = 16f
    override fun getSystemIcon(key: String): IconResource = Icons.AutoMirrored.Filled.Help
}

@Composable
fun LieDetectorPreviewTheme(
    content: @Composable () -> Unit
) {
    val designSystem = DesignSystem(FakeResourceProvider())
    
    CompositionLocalProvider(
        LocalDesignSystem provides designSystem
    ) {
        MaterialTheme {
            Surface(content = content)
        }
    }
}
