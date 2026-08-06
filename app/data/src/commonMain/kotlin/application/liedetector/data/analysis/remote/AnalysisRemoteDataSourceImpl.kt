package application.liedetector.data.analysis.remote

import application.liedetector.engine.network.BaseRemoteDataSource
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.ApiConstants
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
