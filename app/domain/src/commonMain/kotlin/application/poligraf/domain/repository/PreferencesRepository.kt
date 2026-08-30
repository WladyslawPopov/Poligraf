package application.poligraf.domain.repository

import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.domain.model.MarkerShape
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val defaultSkin: Flow<AnalyzerSkin>
    val markerShape: Flow<MarkerShape>
    val isDarkMode: Flow<Boolean>

    fun setDefaultSkin(skin: AnalyzerSkin)
    fun setMarkerShape(shape: MarkerShape)
    fun setDarkMode(isDark: Boolean)
}
