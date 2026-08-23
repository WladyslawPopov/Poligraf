package application.poligraf.engine.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

internal class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {
    override fun setString(key: String, value: String) {
        settings[key] = value
    }

    override fun getString(key: String, defaultValue: String): String {
        return settings.getString(key, defaultValue)
    }

    override fun setBoolean(key: String, value: Boolean) {
        settings[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    override fun remove(key: String) {
        settings.remove(key)
    }
}
