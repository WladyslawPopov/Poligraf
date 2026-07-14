package application.liedetector.theme

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.rounded.*
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DimenToken
import application.liedetector.uicore.theme.IconResource
import application.liedetector.uicore.theme.ResourceProvider

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }

    override fun getColorHex(token: ColorToken): String {
        return when (token) {
            ColorToken.BACKGROUND -> "#121212"
            ColorToken.SURFACE -> "#1E1E1E"
            ColorToken.SURFACE_VARIANT -> "#2C2C2C"
            ColorToken.GLASS_BASE -> "#1AFFFFFF" // Very transparent white
            ColorToken.GLASS_BORDER -> "#33FFFFFF" // Subtle border
            ColorToken.PRIMARY -> "#D1D1D1" 
            ColorToken.ON_PRIMARY -> "#000000"
            ColorToken.TRUTH -> "#00E676" 
            ColorToken.STRESS -> "#FF5252"
            ColorToken.ERROR -> "#B00020"
            ColorToken.TEXT_PRIMARY -> "#FFFFFF"
            ColorToken.TEXT_SECONDARY -> "#A0A0A0"
            ColorToken.TEXT_INVERTED -> "#000000"
        }
    }

    override fun getDimension(token: DimenToken): Float {
        return when (token) {
            DimenToken.MAIN_PADDING -> 16f
            DimenToken.WIDGET_SPACING -> 12f
            DimenToken.CORNER_RADIUS -> 12f
            DimenToken.ICON_SIZE_NAV -> 24f
            DimenToken.HEADER_HEIGHT -> 64f
        }
    }

    override fun getSystemIcon(key: String): IconResource {
        return when(key) {
            "mic" -> Icons.Rounded.Mic
            "history" -> Icons.Rounded.History
            "settings" -> Icons.Rounded.Settings
            "profile" -> Icons.Rounded.AccountCircle
            "chevron_right" -> Icons.Rounded.ChevronRight
            "menu" -> Icons.Rounded.Menu
            else -> Icons.Outlined.HelpOutline
        }
    }
}
