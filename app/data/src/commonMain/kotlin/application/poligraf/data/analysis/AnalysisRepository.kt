package application.poligraf.data.analysis

import application.poligraf.data.analysis.remote.AnalysisRemoteDataSource
import application.poligraf.engine.error.toAppException
import application.poligraf.models.AnalysisRequest
import application.poligraf.models.KmpResult

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
