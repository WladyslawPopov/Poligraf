package application.poligraf.engine.theme

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
interface ThemeManager {
    val isDark: StateFlow<Boolean>
    fun toggleTheme()
    fun setDark(isDark: Boolean)
}


