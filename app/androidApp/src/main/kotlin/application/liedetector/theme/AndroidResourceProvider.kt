package application.liedetector.theme

import android.content.Context
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DimenToken
import application.liedetector.uicore.theme.ResourceProvider

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }

    override fun getColorHex(token: ColorToken): String {
        return when (token) {
            ColorToken.PRIMARY -> "#6200EE"
            ColorToken.SECONDARY -> "#03DAC6"
            ColorToken.BACKGROUND -> "#FFFFFF"
            ColorToken.SURFACE -> "#FFFFFF"
            ColorToken.ERROR -> "#B00020"
            ColorToken.ON_PRIMARY -> "#FFFFFF"
            ColorToken.ON_BACKGROUND -> "#000000"
            ColorToken.TEXT_PRIMARY -> "#000000"
            ColorToken.TEXT_SECONDARY -> "#757575"
            ColorToken.ACCENT_STRESS -> "#FF5722"
            ColorToken.ACCENT_TRUTH -> "#4CAF50"
        }
    }

    override fun getDimension(token: DimenToken): Float {
        return when (token) {
            DimenToken.SPACING_SMALL -> 8f
            DimenToken.SPACING_MEDIUM -> 16f
            DimenToken.SPACING_LARGE -> 24f
            DimenToken.CORNER_RADIUS -> 12f
            DimenToken.ICON_SIZE_SMALL -> 24f
            DimenToken.ICON_SIZE_MEDIUM -> 32f
        }
    }

    override fun getSystemIconName(key: String): String {
        return when(key) {
            "mic" -> "mic"
            "history" -> "history"
            "settings" -> "settings"
            else -> "help"
        }
    }
}
