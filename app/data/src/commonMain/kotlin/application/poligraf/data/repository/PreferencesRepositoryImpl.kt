package application.poligraf.data.repository

import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.domain.model.MarkerShape
import application.poligraf.domain.repository.PreferencesRepository
import application.poligraf.engine.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PreferencesRepositoryImpl(
    private val settingsRepository: SettingsRepository
) : PreferencesRepository {

    private val _defaultSkin = MutableStateFlow(loadDefaultSkin())
    override val defaultSkin: Flow<AnalyzerSkin> = _defaultSkin.asStateFlow()

    private val _markerShape = MutableStateFlow(loadMarkerShape())
    override val markerShape: Flow<MarkerShape> = _markerShape.asStateFlow()

    override fun setDefaultSkin(skin: AnalyzerSkin) {
        settingsRepository.setString(KEY_DEFAULT_SKIN, skin.name)
        _defaultSkin.value = skin
    }

    private fun loadDefaultSkin(): AnalyzerSkin {
        val name = settingsRepository.getString(KEY_DEFAULT_SKIN, AnalyzerSkin.RINGS.name)
        return try {
            AnalyzerSkin.valueOf(name)
        } catch (e: Exception) {
            AnalyzerSkin.RINGS
        }
    }

    override fun setMarkerShape(shape: MarkerShape) {
        settingsRepository.setString(KEY_MARKER_SHAPE, shape.name)
        _markerShape.value = shape
    }

    private fun loadMarkerShape(): MarkerShape {
        val name = settingsRepository.getString(KEY_MARKER_SHAPE, MarkerShape.CIRCLE.name)
        return try {
            MarkerShape.valueOf(name)
        } catch (e: Exception) {
            MarkerShape.CIRCLE
        }
    }

    companion object {
        private const val KEY_DEFAULT_SKIN = "pref_default_skin"
        private const val KEY_MARKER_SHAPE = "pref_marker_shape"
    }
}
