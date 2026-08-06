package application.liedetector.engine.network

import application.liedetector.models.ApiConstants
import io.ktor.client.*

abstract class BaseRemoteDataSource(protected val client: HttpClient) {
    
    /**
     * Builds a full URL for the given endpoint, automatically prepending API_V1.
     * Example: "user/sync" -> "/api/v1/user/sync"
     */
    protected fun endPoint(path: String): String {
        val cleanPath = path.removePrefix("/")
        return "${ApiConstants.API_V1}/$cleanPath"
    }
}
