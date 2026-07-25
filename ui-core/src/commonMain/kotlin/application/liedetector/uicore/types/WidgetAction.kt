package application.liedetector.uicore.types

import kotlinx.serialization.Serializable

@Serializable
enum class WidgetAction {
    START_RECORDING,
    STOP_RECORDING,
    OPEN_HISTORY,
    OPEN_SETTINGS,
    RETRY_ANALYSIS,
    
    // Debug Actions
    DEBUG_TRIGGER_LOADING,
    DEBUG_TRIGGER_ERROR_BLOCKING,
    DEBUG_TRIGGER_ERROR_NON_BLOCKING,
    DEBUG_TRIGGER_SUCCESS_TOAST
}
