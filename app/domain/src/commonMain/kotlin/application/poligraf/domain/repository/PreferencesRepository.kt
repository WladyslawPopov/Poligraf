package application.poligraf.domain.repository

import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.domain.model.MarkerShape
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val defaultSkin: Flow<AnalyzerSkin>
    val markerShape: Flow<MarkerShape>

    fun setDefaultSkin(skin: AnalyzerSkin)
    fun setMarkerShape(shape: MarkerShape)
}
