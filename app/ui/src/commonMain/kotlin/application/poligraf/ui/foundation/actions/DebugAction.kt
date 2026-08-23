package application.poligraf.ui.foundation.actions

sealed class DebugAction : WidgetAction() {
    data object TriggerLoading : DebugAction()
    data object TriggerErrorBlocking : DebugAction()
    data object TriggerErrorNonBlocking : DebugAction()
    data object TriggerSuccessToast : DebugAction()
}
