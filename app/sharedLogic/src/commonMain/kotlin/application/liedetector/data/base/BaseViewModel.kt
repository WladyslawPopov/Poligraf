package application.liedetector.data.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import application.liedetector.data.methods.isIgnorableException
import application.liedetector.data.methods.toErrorType
import application.liedetector.engine.error.AppException
import application.liedetector.engine.error.ErrorType
import application.liedetector.uicore.models.DisplayMetrics
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.ToastType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    override val displayMetrics get() = delegate.displayMetrics

    override fun setLoading(value: Boolean) = delegate.setLoading(value)
    override fun setManualError(type: ErrorType?) = delegate.setManualError(type)
    override fun showToast(token: StringToken, type: ToastType) = delegate.showToast(token, type)
    override fun showRawToast(message: String, type: ToastType) = delegate.showRawToast(message, type)
    override fun setDisplayMetrics(metrics: DisplayMetrics) = delegate.setDisplayMetrics(metrics)
    override fun clearError() = delegate.clearError()
    override fun clearToast() = delegate.clearToast()
    
    protected fun launchSafe(
        isBlocking: Boolean = true,
        block: suspend () -> Unit,
        onFinally: (() -> Unit)? = null
    ) {
        scope.launch {
            try {
                setLoading(true)
                block()
            } catch (e: AppException) {
                handleException(e, e.type, isBlocking)
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
