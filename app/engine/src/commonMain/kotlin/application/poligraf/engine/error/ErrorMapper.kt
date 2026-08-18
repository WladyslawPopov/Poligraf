package application.poligraf.engine.error

fun Throwable.toAppException(): AppException {
    return when (this) {
        is ServerErrorException -> {
            val type = when (errorCode) {
                "401" -> ErrorType.UNAUTHORIZED
                "403" -> ErrorType.FORBIDDEN
                "404" -> ErrorType.NOT_FOUND
                "400" -> ErrorType.INVALID_REQUEST
                "503", "504" -> ErrorType.SERVER_UNAVAILABLE
                "NETWORK_ERROR" -> ErrorType.NO_INTERNET
                else -> ErrorType.UNKNOWN
            }
            AppException(type, humanMessage, errorCode)
        }
        is AppException -> this
        else -> AppException(ErrorType.UNKNOWN, message)
    }
}
