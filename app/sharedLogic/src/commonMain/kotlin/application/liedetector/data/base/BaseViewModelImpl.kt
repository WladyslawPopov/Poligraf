package application.liedetector.data.base

import application.liedetector.engine.error.ErrorType
import application.liedetector.uicore.models.DisplayMetrics
import application.liedetector.uicore.state.ToastState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.ToastType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Delegation implementation of IBaseViewModel.
 */
class BaseViewModelImpl(parentScope: CoroutineScope) : IBaseViewModel {
    override val scope: CoroutineScope = parentScope

    private val _isLoading = MutableStateFlow(false)
    override val isLoading = _isLoading.asStateFlow()

    private val _errorType = MutableStateFlow<ErrorType?>(null)
    override val errorType = _errorType.asStateFlow()

    private val _toastState = MutableStateFlow<ToastState?>(null)
    override val toastState = _toastState.asStateFlow()

    private val _displayMetrics = MutableStateFlow(DisplayMetrics())
    override val displayMetrics = _displayMetrics.asStateFlow()

    override fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    override fun setManualError(type: ErrorType?) {
        _errorType.value = type
    }

    override fun showToast(token: StringToken, type: ToastType) {
        _toastState.value = ToastState(messageToken = token, type = type)
    }

    override fun showRawToast(message: String, type: ToastType) {
        _toastState.value = ToastState(messageRaw = message, type = type)
    }

    override fun setDisplayMetrics(metrics: DisplayMetrics) {
        _displayMetrics.value = metrics
    }

    override fun clearError() {
        _errorType.value = null
    }

    override fun clearToast() {
        _toastState.value = null
    }
}
