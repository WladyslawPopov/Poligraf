package application.poligraf.uicore.theme

import org.jetbrains.compose.resources.StringResource
import application.poligraf.uicore.generated.resources.Res
import application.poligraf.uicore.generated.resources.*

interface IAppStrings {
    val common: ICommonStrings
    val errors: IErrorStrings
    val recorder: IRecorderStrings
    val drawer: IDrawerStrings
    val debug: IDebugStrings
    val subjects: ISubjectStrings
    val toast: IToastStrings
}

interface ICommonStrings {
    val appName: StringResource
    val welcomeText: StringResource
    val record: StringResource
    val history: StringResource
    val settings: StringResource
    val close: StringResource
    val menu: StringResource
    val unknown: StringResource
}

interface IErrorStrings {
    val noInternetTitle: StringResource
    val noInternetMsg: StringResource
    val serverTitle: StringResource
    val serverMsg: StringResource
    val unknownTitle: StringResource
    val unknownMsg: StringResource
    val retry: StringResource
}

interface IRecorderStrings {
    val title: StringResource
    val placeholder: StringResource
    val replace: StringResource
    val trim: StringResource
    val trimCancel: StringResource
    val trimDone: StringResource
    val trimApply: StringResource
    val deletePart: StringResource
    val save: StringResource
    val edit: StringResource
    val uploadFile: StringResource
    val trimMode: StringResource
    val historyTitle: StringResource
    val historySelect: StringResource
    val historyEmpty: StringResource
    val today: StringResource
    val delete: StringResource
    val cancel: StringResource
}

interface IDrawerStrings {
    val settings: StringResource
    val darkMode: StringResource
    val openDebug: StringResource
    val footerTitle: StringResource
    val footerSubtitle: StringResource
}

interface IDebugStrings {
    val title: StringResource
    val dashboard: StringResource
    val triggerLoading: StringResource
    val triggerErrorBlocking: StringResource
    val triggerErrorToast: StringResource
    val triggerSuccessToast: StringResource
    val tabStates: StringResource
    val tabWidgets: StringResource
    val tabLabs: StringResource
    val labsEmpty: StringResource
}

interface ISubjectStrings {
    val newTitle: StringResource
    val newButton: StringResource
    val sectionTemplates: StringResource
    val sectionRecordings: StringResource
    val actionSelected: StringResource
    val deleteConfirmation: StringResource
    val actionDelete: StringResource
    val analysisScore: StringResource
}

interface IToastStrings {
    val authSuccess: StringResource
    val authFailed: StringResource
    val genericWarning: StringResource
}

class AppStringsImpl : IAppStrings {
    override val common = object : ICommonStrings {
        override val appName = Res.string.app_name
        override val welcomeText = Res.string.welcome_text
        override val record = Res.string.record
        override val history = Res.string.recorder_history_title
        override val settings = Res.string.drawer_settings
        override val close = Res.string.close
        override val menu = Res.string.menu
        override val unknown = Res.string.unknown_widget
    }
    
    override val errors = object : IErrorStrings {
        override val noInternetTitle = Res.string.error_no_internet_title
        override val noInternetMsg = Res.string.error_no_internet_msg
        override val serverTitle = Res.string.error_server_title
        override val serverMsg = Res.string.error_server_msg
        override val unknownTitle = Res.string.error_unknown_title
        override val unknownMsg = Res.string.error_unknown_msg
        override val retry = Res.string.error_retry
    }
    
    override val recorder = object : IRecorderStrings {
        override val title = Res.string.recording_screen_title
        override val placeholder = Res.string.recording_screen_placeholder
        override val replace = Res.string.recorder_replace
        override val trim = Res.string.recorder_trim
        override val trimCancel = Res.string.recorder_trim_cancel
        override val trimDone = Res.string.recorder_trim_done
        override val trimApply = Res.string.recorder_trim_apply
        override val deletePart = Res.string.recorder_delete_part
        override val save = Res.string.recorder_save
        override val edit = Res.string.recorder_edit
        override val uploadFile = Res.string.recorder_upload_file
        override val trimMode = Res.string.recorder_trim_mode
        override val historyTitle = Res.string.recorder_history_title
        override val historySelect = Res.string.recorder_history_select
        override val historyEmpty = Res.string.recorder_history_empty
        override val today = Res.string.recorder_today
        override val delete = Res.string.recorder_delete
        override val cancel = Res.string.recorder_cancel
    }
    
    override val drawer = object : IDrawerStrings {
        override val settings = Res.string.drawer_settings
        override val darkMode = Res.string.drawer_dark_mode
        override val openDebug = Res.string.open_debug_sandbox
        override val footerTitle = Res.string.drawer_footer_title
        override val footerSubtitle = Res.string.drawer_footer_subtitle
    }
    
    override val debug = object : IDebugStrings {
        override val title = Res.string.debug_title
        override val dashboard = Res.string.debug_dashboard
        override val triggerLoading = Res.string.debug_trigger_loading
        override val triggerErrorBlocking = Res.string.debug_trigger_error_blocking
        override val triggerErrorToast = Res.string.debug_trigger_error_toast
        override val triggerSuccessToast = Res.string.debug_trigger_success_toast
        override val tabStates = Res.string.tab_states
        override val tabWidgets = Res.string.tab_widgets
        override val tabLabs = Res.string.tab_labs
        override val labsEmpty = Res.string.labs_empty_message
    }
    
    override val subjects = object : ISubjectStrings {
        override val newTitle = Res.string.subject_new_title
        override val newButton = Res.string.subject_new_button
        override val sectionTemplates = Res.string.section_templates
        override val sectionRecordings = Res.string.section_recordings
        override val actionSelected = Res.string.action_selected
        override val deleteConfirmation = Res.string.delete_recording_confirmation
        override val actionDelete = Res.string.action_delete_recording
        override val analysisScore = Res.string.analysis_score
    }

    override val toast = object : IToastStrings {
        override val authSuccess = Res.string.toast_auth_success
        override val authFailed = Res.string.toast_auth_failed
        override val genericWarning = Res.string.toast_generic_warning
    }
}
