package application.liedetector.engine.network.config

interface NetworkConfigProvider {
    val apiBaseUrl: String
    val headers: Map<String, String>
}

class NetworkConfigProviderImpl : NetworkConfigProvider {
    // For Android emulator use 10.0.2.2, for iOS use localhost
    override val apiBaseUrl: String = "http://10.0.2.2:8080" 
    
    override val headers: Map<String, String> = mapOf(
        "Content-Type" to "application/json"
    )
}
