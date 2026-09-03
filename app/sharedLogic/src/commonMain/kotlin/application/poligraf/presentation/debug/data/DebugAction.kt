package application.poligraf.presentation.debug.data

sealed class DebugAction {
    data object TriggerLoading : DebugAction()
    data object TriggerErrorBlocking : DebugAction()
    data object TriggerErrorNonBlocking : DebugAction()
    data object TriggerSuccessToast : DebugAction()
}
