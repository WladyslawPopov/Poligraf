package application.liedetector.data.user

import application.liedetector.data.mapper.toDomain
import application.liedetector.data.user.remote.UserRemoteDataSource
import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.database.CacheRepository
import application.liedetector.engine.error.ServerErrorException
import application.liedetector.domain.error.AppException
import application.liedetector.domain.model.ErrorType
import application.liedetector.domain.model.Subject
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.KmpResult
import application.liedetector.models.SubjectDto
import application.liedetector.models.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import application.liedetector.engine.utils.nowAsEpochSeconds

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

    fun getSubjects(): Flow<List<Subject>>
    
    suspend fun syncSubjects(): KmpResult<Unit>
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

    override fun getSubjects(): Flow<List<Subject>> {
        val cacheKey = "subjects_list"
        val serializer = ListSerializer(SubjectDto.serializer())
        
        return cache.getFlow(cacheKey, serializer).map { cached ->
            cached?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun syncSubjects(): KmpResult<Unit> {
        return try {
            val cacheKey = "subjects_list"
            val serializer = ListSerializer(SubjectDto.serializer())
            
            val remoteSubjects = remote.getSubjects()
            // Update cache (expire in 1 hour)
            cache.put(cacheKey, remoteSubjects, nowAsEpochSeconds() + 3600, serializer)
            KmpResult.Success(Unit)
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }
}
