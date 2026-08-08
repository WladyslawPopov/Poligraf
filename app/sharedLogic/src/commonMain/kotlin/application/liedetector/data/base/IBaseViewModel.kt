package application.liedetector.data.base

import androidx.compose.runtime.Stable
import application.liedetector.engine.error.ErrorType
import application.liedetector.uicore.models.DisplayMetrics
import application.liedetector.uicore.state.ToastState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.ToastType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Base Interface for ViewModels to handle common UI states: Loading, Error, and Toasts.
 */
@Stable
interface IBaseViewModel {
    val scope: CoroutineScope
    val isLoading: StateFlow<Boolean>
    val errorType: StateFlow<ErrorType?>
    val toastState: StateFlow<ToastState?>
    val displayMetrics: StateFlow<DisplayMetrics>
    
    fun setLoading(value: Boolean)
    fun setManualError(type: ErrorType?)
    fun showToast(token: StringToken, type: ToastType)
    fun showRawToast(message: String, type: ToastType)
    fun setDisplayMetrics(metrics: DisplayMetrics)
    fun clearError()
    fun clearToast()
}
