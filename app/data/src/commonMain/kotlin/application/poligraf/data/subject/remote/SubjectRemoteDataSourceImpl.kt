package application.poligraf.data.subject.remote

import application.poligraf.engine.network.BaseRemoteDataSource
import application.poligraf.models.ApiConstants
import application.poligraf.models.SubjectDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class SubjectRemoteDataSourceImpl(client: HttpClient) : BaseRemoteDataSource(client), SubjectRemoteDataSource {
    
    override suspend fun createSubject(subject: SubjectDto): SubjectDto {
        return client.post(endPoint(ApiConstants.ENDPOINT_SUBJECTS)) {
            setBody(subject)
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getSubject(id: String): SubjectDto {
        return client.get(endPoint("${ApiConstants.ENDPOINT_SUBJECTS}/$id")).body()
    }

    override suspend fun getSubjects(): List<SubjectDto> {
        return client.get(endPoint(ApiConstants.ENDPOINT_SUBJECTS)).body()
    }

    override suspend fun deleteSubjects(ids: List<String>): Boolean {
        return client.delete(endPoint(ApiConstants.ENDPOINT_SUBJECTS)) {
            setBody(ids)
            contentType(ContentType.Application.Json)
        }.status.isSuccess()
    }
}
