package application.liedetector.engine.network.config

import io.github.aakira.napier.Napier

interface NetworkConfigProvider {
    val apiBaseUrl: String
    val headers: Map<String, String>
}

class NetworkConfigProviderImpl : NetworkConfigProvider {
    override val apiBaseUrl: String by lazy {
        val url = getDebugUrl()
        Napier.d { "NETWORK: Using API URL -> $url" }
        url
    }
    
    override val headers: Map<String, String> = mapOf(
        "Content-Type" to "application/json"
    )
}

internal fun getDebugUrl(): String {
    // 10.0.2.2 for Android emulator, localhost for iOS simulator
    return if (isAndroidPlatform()) "http://10.0.2.2:8080" else "http://localhost:8080"
}

internal expect fun isAndroidPlatform(): Boolean
