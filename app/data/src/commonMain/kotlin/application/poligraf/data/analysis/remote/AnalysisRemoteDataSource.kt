package application.poligraf.data.analysis.remote

import application.poligraf.models.AnalysisRequest

interface AnalysisRemoteDataSource {
    suspend fun startAnalysis(request: AnalysisRequest): Map<String, String>
}
