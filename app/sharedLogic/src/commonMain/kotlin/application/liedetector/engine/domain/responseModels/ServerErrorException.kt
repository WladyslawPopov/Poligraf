package application.liedetector.engine.domain.responseModels

import application.liedetector.uicore.state.ErrorType

class ServerErrorException(
    val errorCode: String,
    val humanMessage: String
) : Exception("Error $errorCode: $humanMessage") {
    
    val errorType: ErrorType = when(errorCode) {
        "NO_INTERNET" -> ErrorType.NO_INTERNET
        "UNAUTHORIZED" -> ErrorType.UNAUTHORIZED
        "SERVER_DOWN" -> ErrorType.SERVER_UNAVAILABLE
        else -> ErrorType.UNKNOWN
    }
}
