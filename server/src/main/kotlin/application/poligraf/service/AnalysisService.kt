package application.poligraf.service

import application.poligraf.ai.GeminiService
import application.poligraf.database.repository.AnalysisRepository
import application.poligraf.database.repository.UserRepository
import application.poligraf.models.AnalysisRequest
import application.poligraf.models.AnalysisStatus
import application.poligraf.models.Verdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface AnalysisService {
    suspend fun startAnalysis(userId: String, request: AnalysisRequest): Map<String, String>
}

class AnalysisServiceImpl(
    private val userRepository: UserRepository,
    private val analysisRepository: AnalysisRepository,
    private val geminiService: GeminiService,
) : AnalysisService {
    
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun startAnalysis(userId: String, request: AnalysisRequest): Map<String, String> {
        val internalUserId = userRepository.getOrCreateUser(userId, "")
        val analysisId = analysisRepository.createInitialAnalysis(internalUserId, request)
        
        // Launch AI processing in the background
        scope.launch {
            try {
                // In a real scenario, we would process the audio file here
                // and then send the text/features to Gemini.
                // For now, let's just test the AI connection.
                val aiResponse = geminiService.testAi()
                
                // Mocking the result for now
                analysisRepository.updateAnalysisResult(
                    analysisId = analysisId,
                    verdict = Verdict.UNCERTAIN,
                    reasoning = aiResponse
                )
            } catch (e: Exception) {
                e.printStackTrace()
                analysisRepository.updateAnalysisResult(
                    analysisId = analysisId,
                    verdict = Verdict.UNCERTAIN,
                    reasoning = "Error during analysis: ${e.message}"
                )
            }
        }
        
        return mapOf(
            "analysis_id" to analysisId.toString(),
            "status" to AnalysisStatus.PENDING.name
        )
    }
}
