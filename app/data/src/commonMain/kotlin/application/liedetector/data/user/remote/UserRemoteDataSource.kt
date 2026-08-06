package application.liedetector.data.user.remote

import application.liedetector.models.UserDto

interface UserRemoteDataSource {
    suspend fun syncUser(user: UserDto): String
}
