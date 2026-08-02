package application.liedetector.models

object ApiConstants {
    const val AUTH_CONFIG_NAME = "firebase-auth"
    const val API_V1 = "/api/v1"
    const val ENDPOINT_ANALYZE = "/analyze"
    const val ENDPOINT_USER_SYNC = "/user/sync"
    const val ENDPOINT_STATUS = "/"
    
    // Header keys
    const val HEADER_AUTHORIZATION = "Authorization"
    const val BEARER_PREFIX = "Bearer "
}

enum class AnalysisStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

enum class Verdict {
    TRUE,
    LIE,
    UNCERTAIN
}
