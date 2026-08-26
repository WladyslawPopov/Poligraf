package application.poligraf.ui.base

import androidx.compose.runtime.Stable
import application.poligraf.engine.error.ErrorType
import application.poligraf.ui.foundation.models.DisplayMetrics
import application.poligraf.ui.foundation.state.ToastState
import application.poligraf.ui.theme.AppStrings
import application.poligraf.ui.foundation.types.ToastType
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
    fun showToast(provider: (AppStrings) -> String, type: ToastType)
    fun showRawToast(message: String, type: ToastType)
    fun setDisplayMetrics(metrics: DisplayMetrics)
    fun clearError()
    fun clearToast()
}
