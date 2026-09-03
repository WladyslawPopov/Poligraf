package application.poligraf.domain.preferences.repository


import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.MarkerShape
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val skinFlow: Flow<AnalyzerSkin>
    val markerShapeFlow: Flow<MarkerShape>
    val isDarkModeFlow: Flow<Boolean>

    fun setSkin(skin: AnalyzerSkin)
    fun setMarkerShape(shape: MarkerShape)
    fun setDarkMode(isDark: Boolean)
}
