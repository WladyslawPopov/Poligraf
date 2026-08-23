package application.poligraf.engine.network.internal

import application.poligraf.engine.error.ServerErrorException
import application.poligraf.engine.network.config.NetworkConfigProvider
import application.poligraf.engine.config.AppConfig
import application.poligraf.engine.utils.jsonSerializer
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.coroutines.cancellation.CancellationException

internal fun HttpClientConfig<*>.installPlugins(
    networkConfig: NetworkConfigProvider,
    appConfig: AppConfig
) {
    install(ContentNegotiation) {
        json(jsonSerializer)
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (response.status.value >= 300) {
                val errorBody = try { response.bodyAsText() } catch (_: Exception) { "Unknown error" }
                throw ServerErrorException(
                    response.status.value.toString(),
                    errorBody
                )
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            if (cause is CancellationException) throw cause
            if (cause is ServerErrorException) throw cause
            
            throw ServerErrorException(
                errorCode = "NETWORK_ERROR",
                humanMessage = cause.message ?: "Unknown network error"
            )
        }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 30_000
    }

    install(HttpRequestRetry) {
        retryOnException(maxRetries = 2)
        exponentialDelay()
    }

    defaultRequest {
        url(networkConfig.apiBaseUrl)
        networkConfig.headers.forEach { (key, value) ->
            header(key, value)
        }
        // Only Device ID is needed for server as per latest requirement
        header("X-Device-ID", appConfig.deviceId)
    }
}

fun getKtorClient(
    networkConfig: NetworkConfigProvider,
    appConfig: AppConfig
) = HttpClient {
    installPlugins(networkConfig, appConfig)
}
//    .apply {
//    sendPipeline.intercept(HttpSendPipeline.State) {
//        authService.getIdToken()?.let { token ->
//            context.header(ApiConstants.HEADER_AUTHORIZATION, ApiConstants.BEARER_PREFIX + token)
//        }
//    }
//}
