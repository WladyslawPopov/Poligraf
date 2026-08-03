package application.liedetector.uicore.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WidgetAction {
    @Serializable @SerialName("start_recording") data object START_RECORDING : WidgetAction()
    @Serializable @SerialName("stop_recording") data object STOP_RECORDING : WidgetAction()
    @Serializable @SerialName("open_history") data object OPEN_HISTORY : WidgetAction()
    @Serializable @SerialName("open_settings") data object OPEN_SETTINGS : WidgetAction()
    @Serializable @SerialName("open_profile") data object OPEN_PROFILE : WidgetAction()
    @Serializable @SerialName("start_new_investigation") data object START_NEW_INVESTIGATION : WidgetAction()
    @Serializable @SerialName("open_investigation") data class OPEN_INVESTIGATION(val subjectId: String) : WidgetAction()
    @Serializable @SerialName("retry_analysis") data object RETRY_ANALYSIS : WidgetAction()
    
    // Debug Actions
    @Serializable @SerialName("debug_trigger_loading") data object DEBUG_TRIGGER_LOADING : WidgetAction()
    @Serializable @SerialName("debug_trigger_error_blocking") data object DEBUG_TRIGGER_ERROR_BLOCKING : WidgetAction()
    @Serializable @SerialName("debug_trigger_error_non_blocking") data object DEBUG_TRIGGER_ERROR_NON_BLOCKING : WidgetAction()
    @Serializable @SerialName("debug_trigger_success_toast") data object DEBUG_TRIGGER_SUCCESS_TOAST : WidgetAction()
}
