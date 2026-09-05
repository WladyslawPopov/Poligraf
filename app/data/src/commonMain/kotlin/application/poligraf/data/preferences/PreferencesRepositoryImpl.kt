package application.poligraf.data.preferences

import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.analyzer.types.QuantumWindowDuration
import application.poligraf.domain.analyzer.types.SensitivityLevel
import application.poligraf.domain.preferences.repository.PreferencesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PreferencesRepositoryImpl(
    private val settings: Settings,
) : PreferencesRepository {

    private val _skinFlow = MutableStateFlow(
        try {
            AnalyzerSkin.valueOf(settings.getString(KEY_SKIN, AnalyzerSkin.STATE_MAP.name))
        } catch (_: Exception) {
            AnalyzerSkin.STATE_MAP
        }
    )
    override val skinFlow: StateFlow<AnalyzerSkin> = _skinFlow.asStateFlow()

    private val _markerShapeFlow = MutableStateFlow(
        try {
            MarkerShape.valueOf(settings.getString(KEY_MARKER_SHAPE, MarkerShape.CIRCLE.name))
        } catch (_: Exception) {
            MarkerShape.CIRCLE
        }
    )
    override val markerShapeFlow: StateFlow<MarkerShape> = _markerShapeFlow.asStateFlow()

    private val _isDarkModeFlow = MutableStateFlow(settings.getBoolean(KEY_DARK_MODE, true))
    override val isDarkModeFlow: StateFlow<Boolean> = _isDarkModeFlow.asStateFlow()

    private val _sensitivityFlow = MutableStateFlow(
        try {
            SensitivityLevel.valueOf(settings.getString(KEY_SENSITIVITY, SensitivityLevel.MEDIUM.name))
        } catch (_: Exception) {
            SensitivityLevel.MEDIUM
        }
    )
    override val sensitivityFlow: StateFlow<SensitivityLevel> = _sensitivityFlow.asStateFlow()

    private val _quantumWindowFlow = MutableStateFlow(
        try {
            QuantumWindowDuration.valueOf(settings.getString(KEY_QUANTUM_WINDOW, QuantumWindowDuration.TWO_HALF_SEC.name))
        } catch (_: Exception) {
            QuantumWindowDuration.TWO_HALF_SEC
        }
    )
    override val quantumWindowFlow: StateFlow<QuantumWindowDuration> = _quantumWindowFlow.asStateFlow()

    override fun setSkin(skin: AnalyzerSkin) {
        settings.putString(KEY_SKIN, skin.name)
        _skinFlow.value = skin
    }

    override fun setMarkerShape(shape: MarkerShape) {
        settings.putString(KEY_MARKER_SHAPE, shape.name)
        _markerShapeFlow.value = shape
    }

    override fun setDarkMode(isDark: Boolean) {
        settings.putBoolean(KEY_DARK_MODE, isDark)
        _isDarkModeFlow.value = isDark
    }

    override fun setSensitivity(level: SensitivityLevel) {
        settings.putString(KEY_SENSITIVITY, level.name)
        _sensitivityFlow.value = level
    }

    override fun setQuantumWindow(duration: QuantumWindowDuration) {
        settings.putString(KEY_QUANTUM_WINDOW, duration.name)
        _quantumWindowFlow.value = duration
    }

    companion object {
        private const val KEY_SKIN = "pref_key_skin"
        private const val KEY_MARKER_SHAPE = "pref_key_marker_shape"
        private const val KEY_DARK_MODE = "pref_key_dark_mode"
        private const val KEY_SENSITIVITY = "pref_key_sensitivity"
        private const val KEY_QUANTUM_WINDOW = "pref_key_quantum_window"
    }
}
