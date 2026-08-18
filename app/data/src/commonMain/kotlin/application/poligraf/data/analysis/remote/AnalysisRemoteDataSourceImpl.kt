package application.poligraf.data.analysis.remote

import application.poligraf.engine.network.BaseRemoteDataSource
import application.poligraf.models.AnalysisRequest
import application.poligraf.models.ApiConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AnalysisRemoteDataSourceImpl(client: HttpClient) : BaseRemoteDataSource(client), AnalysisRemoteDataSource {
    override suspend fun startAnalysis(request: AnalysisRequest): Map<String, String> {
        return client.post(endPoint(ApiConstants.ENDPOINT_ANALYZE)) {
            setBody(request)
            contentType(ContentType.Application.Json)
        }.body()
    }
}
