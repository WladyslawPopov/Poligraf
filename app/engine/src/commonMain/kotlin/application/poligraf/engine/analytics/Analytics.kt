package application.poligraf.engine.analytics

interface Analytics {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
}
