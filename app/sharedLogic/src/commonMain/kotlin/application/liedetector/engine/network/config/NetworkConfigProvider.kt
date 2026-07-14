package application.liedetector.engine.network.config

interface NetworkConfigProvider {
    val apiBaseUrl: String
    val headers: Map<String, String>
}

class NetworkConfigProviderImpl : NetworkConfigProvider {
    // Determine base URL based on common network rules
    override val apiBaseUrl: String = getDebugUrl()
    
    override val headers: Map<String, String> = mapOf(
        "Content-Type" to "application/json"
    )
}

/**
 * Simple helper to detect platform and return correct local address
 */
internal fun getDebugUrl(): String {
    // On Android (which uses 10.0.2.2 for host) we usually have access to certain system props
    // On iOS we use localhost. For now we use a reliable check.
    return if (isAndroidPlatform()) "http://10.0.2.2:8080" else "http://localhost:8080"
}

// Simple internal check
internal expect fun isAndroidPlatform(): Boolean
