package application.liedetector.domain.error

import application.liedetector.domain.model.ErrorType

class AppException(
    val type: ErrorType,
    val humanMessage: String? = null
) : Exception("App Error [$type]: $humanMessage")
