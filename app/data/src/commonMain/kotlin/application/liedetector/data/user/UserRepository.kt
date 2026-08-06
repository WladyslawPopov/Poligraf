package application.liedetector.data.user

import application.liedetector.data.user.remote.UserRemoteDataSource
import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.error.toAppException
import application.liedetector.models.KmpResult
import application.liedetector.models.UserDto

interface UserRepository {
    suspend fun loginAnonymously(): KmpResult<Unit>
    suspend fun syncUser(metadata: Map<String, String>): KmpResult<String>
}

class UserRepositoryImpl(
    private val remote: UserRemoteDataSource,
    private val authService: AuthService
) : UserRepository {

    override suspend fun loginAnonymously(): KmpResult<Unit> {
        return try {
            if (authService.isAuthorized()) KmpResult.Success(Unit)
            else authService.signInAnonymously()
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }

    override suspend fun syncUser(metadata: Map<String, String>): KmpResult<String> {
        return try {
            KmpResult.Success(remote.syncUser(UserDto(metadata = metadata)))
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }
}
