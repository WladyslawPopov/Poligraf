package application.liedetector.data.analysis.remote

import application.liedetector.models.AnalysisRequest

interface AnalysisRemoteDataSource {
    suspend fun startAnalysis(request: AnalysisRequest): Map<String, String>
}
