package application.liedetector.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import application.liedetector.engine.domain.responseModels.ServerErrorException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base Interface for ViewModels to handle common UI states like loading and errors.
 * Inspired by MarketKMP's IPageKit.
 */
interface IBaseViewModel {
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<ServerErrorException?>
    
    fun setLoading(value: Boolean)
    fun onError(e: ServerErrorException)
    fun clearError()
}

/**
 * Common implementation of IBaseViewModel to be used as a delegate.
 */
class BaseViewModelImpl : IBaseViewModel {
    private val _isLoading = MutableStateFlow(false)
    override val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<ServerErrorException?>(null)
    override val error = _error.asStateFlow()

    override fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    override fun onError(e: ServerErrorException) {
        _error.value = e
    }

    override fun clearError() {
        _error.value = null
    }
}

/**
 * Base class for all ViewModels in LieDetector.
 */
abstract class BaseViewModel : ViewModel(), IBaseViewModel by BaseViewModelImpl() {
    
    protected fun launchSafe(
        block: suspend () -> Unit,
        onError: ((ServerErrorException) -> Unit)? = null,
        onFinally: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                block()
            } catch (e: ServerErrorException) {
                if (onError != null) onError(e) else onError(e)
            } catch (e: Exception) {
                val serverError = ServerErrorException("UNKNOWN", e.message ?: "Unknown error")
                if (onError != null) onError(serverError) else onError(serverError)
            } finally {
                setLoading(false)
                onFinally?.invoke()
            }
        }
    }
}
