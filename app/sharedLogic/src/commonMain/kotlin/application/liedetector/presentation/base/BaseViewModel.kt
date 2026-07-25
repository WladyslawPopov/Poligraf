package application.liedetector.presentation.base

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import application.liedetector.engine.domain.responseModels.ServerErrorException
import application.liedetector.uicore.state.*
import application.liedetector.uicore.theme.StringToken
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Filter for "noisy" network exceptions that shouldn't crash or block the UI.
 */
fun Throwable.isIgnorableException(): Boolean {
    val message = this.message ?: ""
    val name = this::class.simpleName ?: ""

    return this is CancellationException ||
            name.contains("Timeout", ignoreCase = true) ||
            message.contains("Timeout", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("failed to connect to", ignoreCase = true) ||
            message.contains("Failed to connect", ignoreCase = true)
}

/**
 * Maps raw exceptions to high-level ErrorType for consistent UI rendering.
 */
fun Throwable.toErrorType(): ErrorType {
    val message = this.message ?: ""
    return when {
        message.contains("Unable to resolve host", ignoreCase = true) || 
        message.contains("failed to connect", ignoreCase = true) -> ErrorType.NO_INTERNET
        else -> ErrorType.UNKNOWN
    }
}

/**
 * Base Interface for ViewModels to handle common UI states: Loading, Error, and Toasts.
 */
@Stable
interface IBaseViewModel {
    val scope: CoroutineScope
    val isLoading: StateFlow<Boolean>
    val errorType: StateFlow<ErrorType?>
    val toastState: StateFlow<ToastState?>
    
    fun setLoading(value: Boolean)
    fun setManualError(type: ErrorType?)
    fun showToast(token: StringToken, type: ToastType)
    fun showRawToast(message: String, type: ToastType)
    fun clearError()
    fun clearToast()
}

/**
 * Delegation implementation of IBaseViewModel.
 */
class BaseViewModelImpl(private val parentScope: CoroutineScope) : IBaseViewModel {
    override val scope: CoroutineScope = parentScope

    private val _isLoading = MutableStateFlow(false)
    override val isLoading = _isLoading.asStateFlow()

    private val _errorType = MutableStateFlow<ErrorType?>(null)
    override val errorType = _errorType.asStateFlow()

    private val _toastState = MutableStateFlow<ToastState?>(null)
    override val toastState = _toastState.asStateFlow()

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

    override fun clearError() {
        _errorType.value = null
    }

    override fun clearToast() {
        _toastState.value = null
    }
}

/**
 * Base class for all ViewModels. Provides safe coroutine launching with automatic state management.
 */
abstract class BaseViewModel : ViewModel(), IBaseViewModel {
    override val scope: CoroutineScope
        get() = viewModelScope
        
    private val delegate = BaseViewModelImpl(scope)
    
    override val isLoading get() = delegate.isLoading
    override val errorType get() = delegate.errorType
    override val toastState get() = delegate.toastState

    override fun setLoading(value: Boolean) = delegate.setLoading(value)
    override fun setManualError(type: ErrorType?) = delegate.setManualError(type)
    override fun showToast(token: StringToken, type: ToastType) = delegate.showToast(token, type)
    override fun showRawToast(message: String, type: ToastType) = delegate.showRawToast(message, type)
    override fun clearError() = delegate.clearError()
    override fun clearToast() = delegate.clearToast()
    
    /**
     * Executes a network or background task.
     * @param isBlocking If true, shows a full-screen error on failure. If false, shows a Toast.
     */
    protected fun launchSafe(
        isBlocking: Boolean = true,
        block: suspend () -> Unit,
        onFinally: (() -> Unit)? = null
    ) {
        scope.launch {
            try {
                setLoading(true)
                block()
            } catch (e: ServerErrorException) {
                handleException(e, e.errorType, isBlocking)
            } catch (e: Throwable) {
                if (!e.isIgnorableException()) {
                    handleException(e, e.toErrorType(), isBlocking)
                }
            } finally {
                setLoading(false)
                onFinally?.invoke()
            }
        }
    }

    private fun handleException(e: Throwable, type: ErrorType, isBlocking: Boolean) {
        Napier.e(e) { "ViewModel catch error: ${e.message}" }
        if (isBlocking) {
            setManualError(type)
        } else {
            // Non-blocking errors go to Toasts
            val toastType = if (type == ErrorType.NO_INTERNET) ToastType.WARNING else ToastType.ERROR
            val token = when(type) {
                ErrorType.NO_INTERNET -> StringToken.ERROR_NO_INTERNET_TITLE
                ErrorType.SERVER_UNAVAILABLE -> StringToken.ERROR_SERVER_TITLE
                else -> StringToken.ERROR_UNKNOWN_TITLE
            }
            showToast(token, toastType)
        }
    }
}
