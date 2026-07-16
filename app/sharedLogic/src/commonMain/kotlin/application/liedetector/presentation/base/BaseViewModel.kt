package application.liedetector.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import application.liedetector.engine.domain.responseModels.ServerErrorException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
 * Base Interface for ViewModels to handle common UI states like loading and errors.
 */
interface IBaseViewModel {
    val scope : CoroutineScope
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
    override val scope : CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
    override val scope: CoroutineScope
        get() = viewModelScope
    
    protected fun launchSafe(
        block: suspend () -> Unit,
        onFinally: (() -> Unit)? = null
    ) {
        scope.launch {
            try {
                setLoading(true)
                block()
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Throwable) {
                if (e.isIgnorableException()) {
                    e.printStackTrace()
                } else {
                    val serverError = ServerErrorException("UNKNOWN", e.message ?: "Unknown error")
                    onError(serverError)
                }
            } finally {
                setLoading(false)
                onFinally?.invoke()
            }
        }
    }
}
