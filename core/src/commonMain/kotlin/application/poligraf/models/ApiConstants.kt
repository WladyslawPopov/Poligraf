package application.poligraf.models

object ApiConstants {
    const val AUTH_CONFIG_NAME = "firebase-auth"
    const val API_V1 = "api/v1"
    
    // Endpoints
    const val ENDPOINT_ANALYZE = "analyze"
    const val ENDPOINT_USER_SYNC = "user/sync"
    const val ENDPOINT_SUBJECTS = "subjects"
    const val ENDPOINT_STATUS = "status"
    
    // Header keys
    const val HEADER_AUTHORIZATION = "Authorization"
    const val BEARER_PREFIX = "Bearer "

    // Rate Limit Names
    const val RATE_LIMIT_HEAVY = "heavy_api"
}
