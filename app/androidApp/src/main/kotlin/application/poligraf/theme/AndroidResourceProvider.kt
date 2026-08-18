package application.poligraf.theme

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import application.poligraf.R
import application.poligraf.uicore.theme.IconResource
import application.poligraf.uicore.theme.ResourceProvider
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.uicore.theme.tokens.StringToken

class AndroidResourceProvider(private val context: Context) : ResourceProvider {

    override fun getString(token: StringToken): String {
        val resId = when (token) {
            StringToken.APP_NAME -> R.string.app_name
            StringToken.WELCOME_TEXT -> R.string.welcome_text
            StringToken.SUBJECT_NEW_TITLE -> R.string.subject_new_title
            StringToken.SUBJECT_NEW_BUTTON -> R.string.subject_new_button
            StringToken.DRAWER_SETTINGS -> R.string.drawer_settings
            StringToken.DRAWER_DARK_MODE -> R.string.drawer_dark_mode
            StringToken.ERROR_NO_INTERNET_TITLE -> R.string.error_no_internet_title
            StringToken.ERROR_NO_INTERNET_MSG -> R.string.error_no_internet_msg
            StringToken.ERROR_SERVER_TITLE -> R.string.error_server_title
            StringToken.ERROR_SERVER_MSG -> R.string.error_server_msg
            StringToken.ERROR_UNKNOWN_TITLE -> R.string.error_unknown_title
            StringToken.ERROR_UNKNOWN_MSG -> R.string.error_unknown_msg
            StringToken.ERROR_RETRY -> R.string.error_retry
            StringToken.TOAST_AUTH_SUCCESS -> R.string.toast_auth_success
            StringToken.TOAST_AUTH_FAILED -> R.string.toast_auth_failed
            StringToken.TOAST_GENERIC_WARNING -> R.string.toast_generic_warning
            StringToken.RECORD -> R.string.record
            StringToken.ANALYSIS_SCORE -> R.string.analysis_score
            StringToken.UNKNOWN_WIDGET -> R.string.unknown_widget
            StringToken.MENU -> R.string.menu
            StringToken.CLOSE -> R.string.close
            StringToken.DEBUG_TITLE -> R.string.debug_title
            StringToken.DEBUG_TRIGGER_LOADING -> R.string.debug_trigger_loading
            StringToken.DEBUG_TRIGGER_ERROR_BLOCKING -> R.string.debug_trigger_error_blocking
            StringToken.DEBUG_TRIGGER_ERROR_TOAST -> R.string.debug_trigger_error_toast
            StringToken.DEBUG_TRIGGER_SUCCESS_TOAST -> R.string.debug_trigger_success_toast
            StringToken.TAB_STATES -> R.string.tab_states
            StringToken.TAB_WIDGETS -> R.string.tab_widgets
            StringToken.TAB_LABS -> R.string.tab_labs
            StringToken.OPEN_DEBUG_SANDBOX -> R.string.open_debug_sandbox
            StringToken.LABS_EMPTY_MESSAGE -> R.string.labs_empty_message
            StringToken.RECORDING_SCREEN_PLACEHOLDER -> R.string.recording_screen_placeholder
            StringToken.RECORDING_SCREEN_TITLE -> R.string.recording_screen_title
            StringToken.DELETE_RECORDING_CONFIRMATION -> R.string.delete_recording_confirmation
            StringToken.ACTION_DELETE_RECORDING -> R.string.action_delete_recording
            StringToken.ACTION_SELECTED -> R.string.action_selected
            StringToken.SECTION_TEMPLATES -> R.string.section_templates
            StringToken.SECTION_RECORDINGS -> R.string.section_recordings
            StringToken.RECORDER_REPLACE -> R.string.recorder_replace
            StringToken.RECORDER_TRIM -> R.string.recorder_trim
            StringToken.RECORDER_TRIM_CANCEL -> R.string.recorder_trim_cancel
            StringToken.RECORDER_TRIM_DONE -> R.string.recorder_trim_done
            StringToken.RECORDER_DELETE_PART -> R.string.recorder_delete_part
            StringToken.RECORDER_SAVE -> R.string.recorder_save
            StringToken.RECORDER_EDIT -> R.string.recorder_edit
            StringToken.DRAWER_FOOTER_TITLE -> R.string.drawer_footer_title
            StringToken.DRAWER_FOOTER_SUBTITLE -> R.string.drawer_footer_subtitle
            else -> return token.name
        }
        return try {
            context.getString(resId)
        } catch (e: Exception) {
            token.name
        }
    }

    override fun getIcon(token: IconToken): IconResource {
        return when (token) {
            IconToken.MIC -> Icons.Default.Mic
            IconToken.HISTORY -> Icons.Default.History
            IconToken.SETTINGS -> Icons.Default.Settings
            IconToken.PROFILE -> Icons.Default.AccountCircle
            IconToken.CHEVRON_RIGHT -> Icons.Default.ChevronRight
            IconToken.MENU -> Icons.Default.Menu
            IconToken.CLOSE -> Icons.Default.Close
            IconToken.ARROW_BACK -> Icons.AutoMirrored.Filled.ArrowBack
            IconToken.GALLERY -> Icons.Default.Collections
            IconToken.NOTE -> Icons.AutoMirrored.Filled.NoteAdd
            IconToken.DELETE -> Icons.Default.Delete
            IconToken.DRAG_HANDLE -> Icons.Default.DragHandle
            IconToken.EDIT -> Icons.Default.Edit
            IconToken.CHECK -> Icons.Default.Check
            IconToken.MORE_VERT -> Icons.Default.MoreVert
            IconToken.MORE_HORIZ -> Icons.Default.MoreHoriz
            IconToken.PLAY -> Icons.Default.PlayArrow
            IconToken.PAUSE -> Icons.Default.Pause
            IconToken.SKIP_BACK_15 -> Icons.Default.Replay10
            IconToken.SKIP_FORWARD_15 -> Icons.Default.Forward10
            IconToken.TRIM_HANDLE_LEFT -> Icons.Default.West
            IconToken.TRIM_HANDLE_RIGHT -> Icons.Default.East
        }
    }
}
