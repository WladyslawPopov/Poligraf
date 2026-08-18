package application.poligraf.exceptions

sealed class AppException(message: String, val code: String) : Exception(message)

class UserNotFoundException(message: String = "User not found") : AppException(message, "USER_NOT_FOUND")
class SubjectNotFoundException(message: String = "Subject not found") : AppException(message, "SUBJECT_NOT_FOUND")
class UnauthorizedException(message: String = "Unauthorized access") : AppException(message, "UNAUTHORIZED")
class ForbiddenException(message: String = "Access denied") : AppException(message, "FORBIDDEN")
class InvalidRequestException(message: String) : AppException(message, "INVALID_REQUEST")
