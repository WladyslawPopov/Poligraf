package application.poligraf.data.user.remote

import application.poligraf.models.UserDto

interface UserRemoteDataSource {
    suspend fun syncUser(user: UserDto): String
}
