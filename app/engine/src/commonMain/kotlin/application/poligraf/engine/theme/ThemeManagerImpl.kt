package application.poligraf.engine.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ThemeManagerImpl : ThemeManager {
    private val _isDark = MutableStateFlow(true)
    override val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    override fun toggleTheme() {
        _isDark.value = !_isDark.value
    }

    override fun setDark(isDark: Boolean) {
        _isDark.value = isDark
    }
}
