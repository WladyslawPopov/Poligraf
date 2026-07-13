package application.liedetector.data.user

import application.liedetector.data.user.remote.UserRemoteDataSource
import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.database.CacheRepository
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.KmpResult
import application.liedetector.models.SubjectDto

interface UserRepository {
    suspend fun loginAnonymously(): KmpResult<Unit>
    suspend fun startAnalysis(storagePath: String, context: String, subjectId: String?): KmpResult<String>
    suspend fun createSubject(name: String, description: String?): KmpResult<SubjectDto>
}

class UserRepositoryImpl(
    private val remote: UserRemoteDataSource,
    private val authService: AuthService,
    private val cache: CacheRepository
) : UserRepository {

    override suspend fun loginAnonymously(): KmpResult<Unit> {
        if (authService.isAuthorized()) return KmpResult.Success(Unit)
        return authService.signInAnonymously()
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
            KmpResult.Error(e)
        }
    }

    override suspend fun createSubject(name: String, description: String?): KmpResult<SubjectDto> {
        return try {
            val result = remote.createSubject(SubjectDto(name = name, description = description))
            KmpResult.Success(result)
        } catch (e: Throwable) {
            KmpResult.Error(e)
        }
    }
}
