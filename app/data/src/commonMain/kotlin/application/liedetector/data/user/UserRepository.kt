package application.liedetector.data.user

import application.liedetector.data.mapper.toDomain
import application.liedetector.data.user.remote.UserRemoteDataSource
import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.database.CacheRepository
import application.liedetector.engine.error.ServerErrorException
import application.liedetector.domain.error.AppException
import application.liedetector.domain.model.ErrorType
import application.liedetector.domain.model.Subject
import application.liedetector.domain.model.User
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.KmpResult
import application.liedetector.models.SubjectDto
import application.liedetector.models.UserDto

private fun Throwable.toAppException(): AppException {
    return when (this) {
        is ServerErrorException -> {
            val type = when (errorCode) {
                "401" -> ErrorType.UNAUTHORIZED
                "503", "504" -> ErrorType.SERVER_UNAVAILABLE
                "NETWORK_ERROR" -> ErrorType.NO_INTERNET
                else -> ErrorType.UNKNOWN
            }
            AppException(type, humanMessage)
        }
        is AppException -> this
        else -> AppException(ErrorType.UNKNOWN, message)
    }
}

interface UserRepository {
    suspend fun loginAnonymously(): KmpResult<Unit>
    suspend fun syncUser(metadata: Map<String, String>): KmpResult<String>
    suspend fun startAnalysis(storagePath: String, context: String, subjectId: String?): KmpResult<String>
    suspend fun createSubject(
        name: String, 
        avatar: String? = null,
        isDefaultAvatar: Boolean = true,
        description: String? = null
    ): KmpResult<Subject>

    suspend fun getSubject(id: String): KmpResult<Subject>

    suspend fun getSubjects(): KmpResult<List<Subject>>
}

class UserRepositoryImpl(
    private val remote: UserRemoteDataSource,
    private val authService: AuthService,
    private val cache: CacheRepository
) : UserRepository {

    override suspend fun loginAnonymously(): KmpResult<Unit> {
        return try {
            if (authService.isAuthorized()) KmpResult.Success(Unit)
            else authService.signInAnonymously()
        } catch (e: Throwable) {
            throw e.toAppException()
        }
    }

    override suspend fun syncUser(metadata: Map<String, String>): KmpResult<String> {
        return try {
            KmpResult.Success(remote.syncUser(UserDto(metadata = metadata)))
        } catch (e: Throwable) {
            throw e.toAppException()
        }
    }

    override suspend fun startAnalysis(
        storagePath: String, 
        context: String, 
        subjectId: String?
    ): KmpResult<String> {
        return try {
            val response = remote.startAnalysis(
                AnalysisRequest(storagePath, context, subjectId)
            )
            val analysisId = response["analysis_id"] ?: throw Exception("Missing analysis_id")
            KmpResult.Success(analysisId)
        } catch (e: Throwable) {
            throw e.toAppException()
        }
    }

    override suspend fun createSubject(
        name: String, 
        avatar: String?,
        isDefaultAvatar: Boolean,
        description: String?
    ): KmpResult<Subject> {
        return try {
            val result = remote.createSubject(
                SubjectDto(
                    name = name, 
                    avatar = avatar,
                    isDefaultAvatar = isDefaultAvatar,
                    description = description
                )
            )
            KmpResult.Success(result.toDomain())
        } catch (e: Throwable) {
            throw e.toAppException()
        }
    }

    override suspend fun getSubject(id: String): KmpResult<Subject> {
        return try {
            KmpResult.Success(remote.getSubject(id).toDomain())
        } catch (e: Throwable) {
            throw e.toAppException()
        }
    }

    override suspend fun getSubjects(): KmpResult<List<Subject>> {
        return try {
            KmpResult.Success(remote.getSubjects().map { it.toDomain() })
        } catch (e: Throwable) {
            throw e.toAppException()
        }
    }
}
