package application.poligraf.data.preferences

import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.preferences.repository.PreferencesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    override val skinFlow: Flow<AnalyzerSkin> = _skinFlow.asStateFlow()

    private val _markerShapeFlow = MutableStateFlow(
        try {
            MarkerShape.valueOf(settings.getString(KEY_MARKER_SHAPE, MarkerShape.CIRCLE.name))
        } catch (_: Exception) {
            MarkerShape.CIRCLE
        }
    )
    override val markerShapeFlow: Flow<MarkerShape> = _markerShapeFlow.asStateFlow()

    private val _isDarkModeFlow = MutableStateFlow(settings.getBoolean(KEY_DARK_MODE, true))
    override val isDarkModeFlow: Flow<Boolean> = _isDarkModeFlow.asStateFlow()

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

    companion object {
        private const val KEY_SKIN = "pref_key_skin"
        private const val KEY_MARKER_SHAPE = "pref_key_marker_shape"
        private const val KEY_DARK_MODE = "pref_key_dark_mode"
    }
}
