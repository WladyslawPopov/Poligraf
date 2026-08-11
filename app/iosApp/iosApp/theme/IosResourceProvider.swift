import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    func getString(token: StringToken) -> String {
        let key: String
        switch token {
        case .appName: key = "app_name"
        case .welcomeText: key = "welcome_text"
        case .subjectNewTitle: key = "subject_new_title"
        case .subjectNewButton: key = "subject_new_button"
        case .drawerSettings: key = "drawer_settings"
        case .drawerDarkMode: key = "drawer_dark_mode"
        
        case .errorNoInternetTitle: key = "error_no_internet_title"
        case .errorNoInternetMsg: key = "error_no_internet_msg"
        case .errorServerTitle: key = "error_server_title"
        case .errorServerMsg: key = "error_server_msg"
        case .errorUnknownTitle: key = "error_unknown_title"
        case .errorUnknownMsg: key = "error_unknown_msg"
        case .errorRetry: key = "error_retry"
        
        case .toastAuthSuccess: key = "toast_auth_success"
        case .toastAuthFailed: key = "toast_auth_failed"
        case .toastGenericWarning: key = "toast_generic_warning"

        case .record: key = "record"
        case .analysisScore: key = "analysis_score"
        case .unknownWidget: key = "unknown_widget"
        case .menu: key = "menu"
        case .close: key = "close"

        case .debugTitle: key = "debug_title"
        case .debugTriggerLoading: key = "debug_trigger_loading"
        case .debugTriggerErrorBlocking: key = "debug_trigger_error_blocking"
        case .debugTriggerErrorToast: key = "debug_trigger_error_toast"
        case .debugTriggerSuccessToast: key = "debug_trigger_success_toast"

        case .debugDashboard: key = "debug_title"
        case .tabStates: key = "tab_states"
        case .tabWidgets: key = "tab_widgets"
        case .tabLabs: key = "tab_labs"
        case .openDebugSandbox: key = "open_debug_sandbox"
        case .labsEmptyMessage: key = "labs_empty_message"
        case .recordingScreenPlaceholder: key = "recording_screen_placeholder"
        case .recordingScreenTitle: key = "recording_screen_title"
        case .deleteRecordingConfirmation: key = "delete_recording_confirmation"
        case .actionDeleteRecording: key = "action_delete_recording"
        case .actionSelected: key = "action_selected"
        case .sectionTemplates: key = "section_templates"
        case .sectionRecordings: key = "section_recordings"
        case .drawerFooterTitle: key = "drawer_footer_title"
        case .drawerFooterSubtitle: key = "drawer_footer_subtitle"
        
        case .recorderReplace: key = "recorder_replace"
        case .recorderTrim: key = "recorder_trim"
        case .recorderTrimCancel: key = "recorder_trim_cancel"
        case .recorderTrimDone: key = "recorder_trim_done"
        case .recorderDeletePart: key = "recorder_delete_part"
        case .recorderSave: key = "recorder_save"
        case .recorderEdit: key = "recorder_edit"
        case .recorderUploadFile: key = "recorder_upload_file"
        case .recorderTrimMode: key = "recorder_trim_mode"
        case .recorderHistoryTitle: key = "recorder_history_title"
        case .recorderHistorySelect: key = "recorder_history_select"
        case .recorderHistoryEmpty: key = "recorder_history_empty"
        case .recorderToday: key = "recorder_today"
        case .recorderCancel: key = "recorder_cancel"
        case .recorderTrimApply: key = "recorder_trim_apply"
        case .recorderDelete: key = "recorder_delete"
        default: key = ""
        }
        return NSLocalizedString(key, comment: "")
    }
    
    func getIcon(token: IconToken) -> String {
        switch token {
        case .mic: return "mic.fill"
        case .history: return "clock.fill"
        case .settings: return "gearshape.fill"
        case .profile: return "person.circle.fill"
        case .chevronRight: return "chevron.right"
        case .menu: return "line.3.horizontal"
        case .close: return "xmark"
        case .arrowBack: return "chevron.left"
        case .gallery: return "photo.on.rectangle"
        case .note: return "text.quote"
        case .delete: return "trash.fill"
        case .dragHandle: return "line.3.horizontal"
        case .edit: return "pencil"
        case .check: return "checkmark.circle.fill"
        case .moreVert: return "ellipsis.circle"
        case .play: return "play.fill"
        case .pause: return "pause.fill"
        case .skipBack15: return "gobackward.15"
        case .skipForward15: return "goforward.15"
        case .trimHandleLeft: return "chevron.left.circle.fill"
        case .trimHandleRight: return "chevron.right.circle.fill"
        }
    }
    
    func getColorHex(token: ColorToken, isDark: Bool) -> String {
        return ThemeDefaults.shared.getColorHex(token: token, isDark: isDark)
    }
    
    func getDimension(token: DimenToken) -> Float {
        return ThemeDefaults.shared.getDimension(token: token)
    }
}
