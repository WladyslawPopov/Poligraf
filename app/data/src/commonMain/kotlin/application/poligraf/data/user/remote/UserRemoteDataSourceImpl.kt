package application.poligraf.data.user.remote

import application.poligraf.engine.network.BaseRemoteDataSource
import application.poligraf.models.ApiConstants
import application.poligraf.models.UserDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class UserRemoteDataSourceImpl(client: HttpClient) : BaseRemoteDataSource(client), UserRemoteDataSource {
    
    override suspend fun syncUser(user: UserDto): String {
        val response: Map<String, String> = client.post(endPoint(ApiConstants.ENDPOINT_USER_SYNC)) {
            setBody(user)
            contentType(ContentType.Application.Json)
        }.body()
        return response["user_id"] ?: ""
    }
}
