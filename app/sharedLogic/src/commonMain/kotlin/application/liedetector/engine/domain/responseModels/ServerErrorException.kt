package application.liedetector.engine.domain.responseModels

class ServerErrorException(
    val errorCode: String,
    val humanMessage: String
) : Exception("Error $errorCode: $humanMessage")
