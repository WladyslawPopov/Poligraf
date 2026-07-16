package application.liedetector.theme

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.rounded.*
import application.liedetector.uicore.theme.IconResource
import application.liedetector.uicore.theme.ResourceProvider

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
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
