package application.liedetector.data.user.remote

import application.liedetector.models.AnalysisRequest
import application.liedetector.models.ApiConstants
import application.liedetector.models.SubjectDto
import application.liedetector.models.UserDto
import application.liedetector.uicore.widgets.UiWidget
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

interface UserRemoteDataSource {
    suspend fun startAnalysis(request: AnalysisRequest): Map<String, String>
    suspend fun createSubject(subject: SubjectDto): SubjectDto
    suspend fun getSubject(id: String): SubjectDto
    suspend fun getSubjects(): List<SubjectDto>
    suspend fun syncUser(user: UserDto): String
}

class UserRemoteDataSourceImpl(private val client: HttpClient) : UserRemoteDataSource {
    override suspend fun startAnalysis(request: AnalysisRequest): Map<String, String> {
        return client.post("${ApiConstants.API_V1}${ApiConstants.ENDPOINT_ANALYZE}") {
            setBody(request)
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun createSubject(subject: SubjectDto): SubjectDto {
        return client.post("${ApiConstants.API_V1}/subject") {
            setBody(subject)
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getSubject(id: String): SubjectDto {
        return client.get("${ApiConstants.API_V1}/subject/$id").body()
    }

    override suspend fun getSubjects(): List<SubjectDto> {
        return client.get("${ApiConstants.API_V1}/subjects").body()
    }

    override suspend fun syncUser(user: UserDto): String {
        val response: Map<String, String> = client.post("${ApiConstants.API_V1}${ApiConstants.ENDPOINT_USER_SYNC}") {
            setBody(user)
            contentType(ContentType.Application.Json)
        }.body()
        return response["user_id"] ?: ""
    }
}
