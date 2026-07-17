package application.liedetector.theme

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.rounded.*
import application.liedetector.uicore.theme.StringToken
import application.liedetector.uicore.theme.IconResource
import application.liedetector.uicore.theme.ResourceProvider

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(token: StringToken): String {
        val resId = when(token) {
            StringToken.WELCOME_TITLE -> application.liedetector.R.string.welcome_title
            StringToken.WELCOME_SUBTITLE -> application.liedetector.R.string.welcome_subtitle
            StringToken.START_INVESTIGATION -> application.liedetector.R.string.start_investigation
            StringToken.DRAWER_SETTINGS -> application.liedetector.R.string.drawer_settings
            StringToken.DRAWER_DARK_MODE -> application.liedetector.R.string.drawer_dark_mode
            
            StringToken.ERROR_NO_INTERNET_TITLE -> application.liedetector.R.string.error_no_internet_title
            StringToken.ERROR_NO_INTERNET_MSG -> application.liedetector.R.string.error_no_internet_msg
            StringToken.ERROR_SERVER_TITLE -> application.liedetector.R.string.error_server_title
            StringToken.ERROR_SERVER_MSG -> application.liedetector.R.string.error_server_msg
            StringToken.ERROR_UNKNOWN_TITLE -> application.liedetector.R.string.error_unknown_title
            StringToken.ERROR_UNKNOWN_MSG -> application.liedetector.R.string.error_unknown_msg
            StringToken.ERROR_RETRY -> application.liedetector.R.string.error_retry
            
            StringToken.TOAST_AUTH_SUCCESS -> application.liedetector.R.string.toast_auth_success
            StringToken.TOAST_AUTH_FAILED -> application.liedetector.R.string.toast_auth_failed
            StringToken.TOAST_GENERIC_WARNING -> application.liedetector.R.string.toast_generic_warning
        }
        return context.getString(resId)
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
