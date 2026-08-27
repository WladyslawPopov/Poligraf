package application.poligraf.engine.settings

import application.poligraf.engine.models.AnalyzerSkin
import application.poligraf.engine.models.MarkerShape
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceManager(private val repository: SettingsRepository) {

    private val _defaultSkin = MutableStateFlow(loadDefaultSkin())
    val defaultSkin: Flow<AnalyzerSkin> = _defaultSkin.asStateFlow()

    private val _markerShape = MutableStateFlow(loadMarkerShape())
    val markerShape: Flow<MarkerShape> = _markerShape.asStateFlow()

    fun setDefaultSkin(skin: AnalyzerSkin) {
        repository.setString(KEY_DEFAULT_SKIN, skin.name)
        _defaultSkin.value = skin
    }

    private fun loadDefaultSkin(): AnalyzerSkin {
        val name = repository.getString(KEY_DEFAULT_SKIN, AnalyzerSkin.RINGS.name)
        return try { AnalyzerSkin.valueOf(name) } catch (e: Exception) { AnalyzerSkin.RINGS }
    }

    fun setMarkerShape(shape: MarkerShape) {
        repository.setString(KEY_MARKER_SHAPE, shape.name)
        _markerShape.value = shape
    }

    private fun loadMarkerShape(): MarkerShape {
        val name = repository.getString(KEY_MARKER_SHAPE, MarkerShape.CIRCLE.name)
        return try { MarkerShape.valueOf(name) } catch (e: Exception) { MarkerShape.CIRCLE }
    }

    companion object {
        private const val KEY_DEFAULT_SKIN = "pref_default_skin"
        private const val KEY_MARKER_SHAPE = "pref_marker_shape"
    }
}
