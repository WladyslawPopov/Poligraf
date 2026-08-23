package application.poligraf.engine.settings

interface SettingsRepository {
    fun setString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
    fun setBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun remove(key: String)
}
