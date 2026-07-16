package application.liedetector.theme


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple global state holder for the theme on Android.
 */
object ThemeState {
    private val _isDark = MutableStateFlow(true)
    var isDark : StateFlow<Boolean> = _isDark.asStateFlow()
    
    fun toggle() {
        _isDark.value = !isDark.value
    }
}
