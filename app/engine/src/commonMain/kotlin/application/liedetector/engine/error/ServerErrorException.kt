package application.liedetector.engine.error

class ServerErrorException(
    val errorCode: String,
    val humanMessage: String
) : Exception("Error $errorCode: $humanMessage")
