package application.poligraf.engine.network.config

interface NetworkConfigProvider {
    val apiBaseUrl: String
    val headers: Map<String, String>
}
