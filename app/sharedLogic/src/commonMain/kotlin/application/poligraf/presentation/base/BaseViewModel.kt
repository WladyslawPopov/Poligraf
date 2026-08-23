package application.poligraf.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import application.poligraf.data.methods.isIgnorableException
import application.poligraf.data.methods.toErrorType
import application.poligraf.engine.error.AppException
import application.poligraf.engine.error.ErrorType
import application.poligraf.ui.base.IBaseViewModel
import application.poligraf.ui.foundation.models.DisplayMetrics
import application.poligraf.ui.theme.AppStrings
import application.poligraf.ui.foundation.state.ToastType
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
    override fun showToast(provider: (AppStrings) -> String, type: ToastType) = delegate.showToast(provider, type)
    override fun showRawToast(message: String, type: ToastType) = delegate.showRawToast(message, type)
    override fun setDisplayMetrics(metrics: DisplayMetrics) = delegate.setDisplayMetrics(metrics)
    override fun clearError() = delegate.clearError()
    override fun clearToast() = delegate.clearToast()
    
    protected fun launchSafe(
        isBlocking: Boolean = true,
        onFinally: (() -> Unit)? = null,
        block: suspend () -> Unit
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
            val provider: (AppStrings) -> String = when(type) {
                else -> { strings -> strings.errors.message}
            }
            showToast(provider, toastType)
        }
    }
}
