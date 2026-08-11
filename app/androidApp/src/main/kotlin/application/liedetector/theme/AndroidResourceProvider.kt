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
        return when(token) {
            StringToken.APP_NAME -> context.getString(application.liedetector.R.string.app_name)
            StringToken.WELCOME_TEXT -> context.getString(application.liedetector.R.string.welcome_text)
            StringToken.SUBJECT_NEW_TITLE -> context.getString(application.liedetector.R.string.subject_new_title)
            StringToken.SUBJECT_NEW_BUTTON -> context.getString(application.liedetector.R.string.subject_new_button)
            StringToken.DRAWER_SETTINGS -> context.getString(application.liedetector.R.string.drawer_settings)
            StringToken.DRAWER_DARK_MODE -> context.getString(application.liedetector.R.string.drawer_dark_mode)
            
            StringToken.ERROR_NO_INTERNET_TITLE -> context.getString(application.liedetector.R.string.error_no_internet_title)
            StringToken.ERROR_NO_INTERNET_MSG -> context.getString(application.liedetector.R.string.error_no_internet_msg)
            StringToken.ERROR_SERVER_TITLE -> context.getString(application.liedetector.R.string.error_server_title)
            StringToken.ERROR_SERVER_MSG -> context.getString(application.liedetector.R.string.error_server_msg)
            StringToken.ERROR_UNKNOWN_TITLE -> context.getString(application.liedetector.R.string.error_unknown_title)
            StringToken.ERROR_UNKNOWN_MSG -> context.getString(application.liedetector.R.string.error_unknown_msg)
            StringToken.ERROR_RETRY -> context.getString(application.liedetector.R.string.error_retry)
            
            StringToken.TOAST_AUTH_SUCCESS -> context.getString(application.liedetector.R.string.toast_auth_success)
            StringToken.TOAST_AUTH_FAILED -> context.getString(application.liedetector.R.string.toast_auth_failed)
            StringToken.TOAST_GENERIC_WARNING -> context.getString(application.liedetector.R.string.toast_generic_warning)

            StringToken.RECORD -> context.getString(application.liedetector.R.string.record)
            StringToken.ANALYSIS_SCORE -> context.getString(application.liedetector.R.string.analysis_score)
            StringToken.UNKNOWN_WIDGET -> context.getString(application.liedetector.R.string.unknown_widget)
            StringToken.MENU -> context.getString(application.liedetector.R.string.menu)
            StringToken.CLOSE -> context.getString(application.liedetector.R.string.close)

            StringToken.DEBUG_TITLE -> context.getString(application.liedetector.R.string.debug_title)
            StringToken.DEBUG_TRIGGER_LOADING -> context.getString(application.liedetector.R.string.debug_trigger_loading)
            StringToken.DEBUG_TRIGGER_ERROR_BLOCKING -> context.getString(application.liedetector.R.string.debug_trigger_error_blocking)
            StringToken.DEBUG_TRIGGER_ERROR_TOAST -> context.getString(application.liedetector.R.string.debug_trigger_error_toast)
            StringToken.DEBUG_TRIGGER_SUCCESS_TOAST -> context.getString(application.liedetector.R.string.debug_trigger_success_toast)

            StringToken.DEBUG_DASHBOARD -> context.getString(application.liedetector.R.string.debug_title)
            StringToken.TAB_STATES -> context.getString(application.liedetector.R.string.tab_states)
            StringToken.TAB_WIDGETS -> context.getString(application.liedetector.R.string.tab_widgets)
            StringToken.TAB_LABS -> context.getString(application.liedetector.R.string.tab_labs)
            StringToken.OPEN_DEBUG_SANDBOX -> context.getString(application.liedetector.R.string.open_debug_sandbox)
            StringToken.LABS_EMPTY_MESSAGE -> context.getString(application.liedetector.R.string.labs_empty_message)
            StringToken.RECORDING_SCREEN_PLACEHOLDER -> context.getString(application.liedetector.R.string.recording_screen_placeholder)
            StringToken.RECORDING_SCREEN_TITLE -> context.getString(application.liedetector.R.string.recording_screen_title)
            StringToken.DELETE_RECORDING_CONFIRMATION -> context.getString(application.liedetector.R.string.delete_recording_confirmation)
            StringToken.ACTION_DELETE_RECORDING -> context.getString(application.liedetector.R.string.action_delete_recording)
            StringToken.ACTION_SELECTED -> context.getString(application.liedetector.R.string.action_selected)
            StringToken.SECTION_TEMPLATES -> context.getString(application.liedetector.R.string.section_templates)
            StringToken.SECTION_RECORDINGS -> context.getString(application.liedetector.R.string.section_recordings)
            
            StringToken.DRAWER_FOOTER_TITLE -> context.getString(application.liedetector.R.string.drawer_footer_title)
            StringToken.DRAWER_FOOTER_SUBTITLE -> context.getString(application.liedetector.R.string.drawer_footer_subtitle)

            StringToken.RECORDER_REPLACE -> context.getString(application.liedetector.R.string.recorder_replace)
            StringToken.RECORDER_TRIM -> context.getString(application.liedetector.R.string.recorder_trim)
            StringToken.RECORDER_TRIM_CANCEL -> context.getString(application.liedetector.R.string.recorder_trim_cancel)
            StringToken.RECORDER_TRIM_DONE -> context.getString(application.liedetector.R.string.recorder_trim_done)
            StringToken.RECORDER_DELETE_PART -> context.getString(application.liedetector.R.string.recorder_delete_part)
            StringToken.RECORDER_SAVE -> context.getString(application.liedetector.R.string.recorder_save)
            StringToken.RECORDER_EDIT -> context.getString(application.liedetector.R.string.recorder_edit)
            StringToken.RECORDER_UPLOAD_FILE -> "Upload audio from file"
            StringToken.RECORDER_TRIM_MODE -> "Trim recording"
            StringToken.RECORDER_HISTORY_TITLE -> "All Recordings"
            StringToken.RECORDER_HISTORY_SELECT -> "Select"
            StringToken.RECORDER_HISTORY_EMPTY -> "Make your first\nrecord 🎙️"
            StringToken.RECORDER_TODAY -> "Today"
            StringToken.RECORDER_CANCEL -> "Cancel"
            StringToken.RECORDER_TRIM_APPLY -> "Apply"
            StringToken.RECORDER_DELETE -> "Delete"
        }
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
            IconToken.GALLERY -> Icons.Rounded.Image
            IconToken.NOTE -> Icons.Rounded.Description
            IconToken.DELETE -> Icons.Rounded.Delete
            IconToken.DRAG_HANDLE -> Icons.Rounded.DragHandle
            IconToken.EDIT -> Icons.Rounded.Edit
            IconToken.CHECK -> Icons.Rounded.CheckCircle
            IconToken.MORE_VERT -> Icons.Rounded.MoreVert
            IconToken.PLAY -> Icons.Rounded.PlayArrow
            IconToken.PAUSE -> Icons.Rounded.Pause
            IconToken.SKIP_BACK_15 -> Icons.Rounded.Replay10 // Approximation
            IconToken.SKIP_FORWARD_15 -> Icons.Rounded.Forward10 // Approximation
            IconToken.TRIM_HANDLE_LEFT -> Icons.AutoMirrored.Rounded.KeyboardArrowLeft
            IconToken.TRIM_HANDLE_RIGHT -> Icons.AutoMirrored.Rounded.KeyboardArrowRight
        }
    }
}
