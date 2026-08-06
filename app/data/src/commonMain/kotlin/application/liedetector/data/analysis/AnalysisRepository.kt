package application.liedetector.data.analysis

import application.liedetector.data.analysis.remote.AnalysisRemoteDataSource
import application.liedetector.engine.error.toAppException
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.KmpResult

interface AnalysisRepository {
    suspend fun startAnalysis(storagePath: String, context: String, subjectId: String?): KmpResult<String>
}

class AnalysisRepositoryImpl(
    private val remote: AnalysisRemoteDataSource
) : AnalysisRepository {

    override suspend fun startAnalysis(
        storagePath: String, 
        context: String, 
        subjectId: String?
    ): KmpResult<String> {
        return try {
            val response = remote.startAnalysis(
                AnalysisRequest(storagePath, context, subjectId)
            )
            val analysisId = response["analysis_id"] ?: throw Exception("Missing analysis_id")
            KmpResult.Success(analysisId)
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }
}
