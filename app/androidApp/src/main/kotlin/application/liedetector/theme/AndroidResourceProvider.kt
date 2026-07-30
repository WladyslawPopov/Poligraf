package application.liedetector.theme

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import application.liedetector.uicore.theme.tokens.*
import application.liedetector.uicore.theme.IconResource
import application.liedetector.uicore.theme.ResourceProvider

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(token: StringToken): String {
        val resId = when(token) {
            StringToken.APP_NAME -> application.liedetector.R.string.app_name
            StringToken.WELCOME_TEXT -> application.liedetector.R.string.welcome_text
            StringToken.SUBJECT_NEW_TITLE -> application.liedetector.R.string.subject_new_title
            StringToken.SUBJECT_NEW_BUTTON -> application.liedetector.R.string.subject_new_button
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

            StringToken.RECORD -> application.liedetector.R.string.record
            StringToken.ANALYSIS_SCORE -> application.liedetector.R.string.analysis_score
            StringToken.UNKNOWN_WIDGET -> application.liedetector.R.string.unknown_widget
            StringToken.MENU -> application.liedetector.R.string.menu
            StringToken.CLOSE -> application.liedetector.R.string.close

            StringToken.DEBUG_TITLE -> application.liedetector.R.string.debug_title
            StringToken.DEBUG_TRIGGER_LOADING -> application.liedetector.R.string.debug_trigger_loading
            StringToken.DEBUG_TRIGGER_ERROR_BLOCKING -> application.liedetector.R.string.debug_trigger_error_blocking
            StringToken.DEBUG_TRIGGER_ERROR_TOAST -> application.liedetector.R.string.debug_trigger_error_toast
            StringToken.DEBUG_TRIGGER_SUCCESS_TOAST -> application.liedetector.R.string.debug_trigger_success_toast

            StringToken.DEBUG_DASHBOARD -> application.liedetector.R.string.debug_title
            StringToken.TAB_STATES -> application.liedetector.R.string.tab_states
            StringToken.TAB_WIDGETS -> application.liedetector.R.string.tab_widgets
            StringToken.TAB_LABS -> application.liedetector.R.string.tab_labs
            StringToken.OPEN_DEBUG_SANDBOX -> application.liedetector.R.string.open_debug_sandbox
            StringToken.LABS_EMPTY_MESSAGE -> application.liedetector.R.string.labs_empty_message
            StringToken.INVESTIGATION_SCREEN_PLACEHOLDER -> application.liedetector.R.string.investigation_screen_placeholder
        }
        return context.getString(resId)
    }

    override fun getIcon(token: IconToken): IconResource {
        return when(token) {
            IconToken.MIC -> Icons.Rounded.Mic
            IconToken.HISTORY -> Icons.Rounded.History
            IconToken.SETTINGS -> Icons.Rounded.Settings
            IconToken.PROFILE -> Icons.Rounded.AccountCircle
            IconToken.CHEVRON_RIGHT -> Icons.Rounded.ChevronRight
            IconToken.MENU -> Icons.Rounded.Menu
            IconToken.CLOSE -> Icons.Rounded.Close
            IconToken.ARROW_BACK -> Icons.AutoMirrored.Rounded.ArrowBack
        }
    }
}
