package application.liedetector.uicore.theme

import kotlinx.serialization.Serializable

@Serializable
enum class StringToken {
    APP_NAME,
    WELCOME_TITLE,
    WELCOME_SUBTITLE,
    START_INVESTIGATION,
    DRAWER_SETTINGS,
    DRAWER_DARK_MODE,
    
    // Error States
    ERROR_NO_INTERNET_TITLE,
    ERROR_NO_INTERNET_MSG,
    ERROR_SERVER_TITLE,
    ERROR_SERVER_MSG,
    ERROR_UNKNOWN_TITLE,
    ERROR_UNKNOWN_MSG,
    ERROR_RETRY,
    
    // Toast Notifications
    TOAST_AUTH_SUCCESS,
    TOAST_AUTH_FAILED,
    TOAST_GENERIC_WARNING,

    // UI Elements
    RECORD,
    ANALYSIS_SCORE,
    UNKNOWN_WIDGET,
    MENU,
    CLOSE,

    // Debug Dashboard
    DEBUG_TITLE,
    DEBUG_TRIGGER_LOADING,
    DEBUG_TRIGGER_ERROR_BLOCKING,
    DEBUG_TRIGGER_ERROR_TOAST,
    DEBUG_TRIGGER_SUCCESS_TOAST,

    DEBUG_DASHBOARD,
    TAB_STATES,
    TAB_WIDGETS,
    TAB_LABS,
    OPEN_DEBUG_SANDBOX
}
