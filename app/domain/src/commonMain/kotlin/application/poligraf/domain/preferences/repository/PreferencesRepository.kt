package application.poligraf.domain.preferences.repository

import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.analyzer.types.QuantumWindowDuration
import application.poligraf.domain.analyzer.types.SensitivityLevel
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val skinFlow: StateFlow<AnalyzerSkin>
    val markerShapeFlow: StateFlow<MarkerShape>
    val isDarkModeFlow: StateFlow<Boolean>
    val sensitivityFlow: StateFlow<SensitivityLevel>
    val quantumWindowFlow: StateFlow<QuantumWindowDuration>

    fun setSkin(skin: AnalyzerSkin)
    fun setMarkerShape(shape: MarkerShape)
    fun setDarkMode(isDark: Boolean)
    fun setSensitivity(level: SensitivityLevel)
    fun setQuantumWindow(duration: QuantumWindowDuration)
}
