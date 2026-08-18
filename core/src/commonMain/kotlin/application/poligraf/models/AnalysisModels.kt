package application.poligraf.models

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisRequest(
    val storagePath: String,
    val contextText: String,
    val subjectId: String? = null
)

@Serializable
data class AnalysisResponse(
    val id: String,
    val verdict: String,
    val deceptionProbability: Int,
    val acousticStressScore: Int,
    val manipulationScore: Int,
    val logicConsistencyScore: Int,
    val reasoning: String,
    val createdAt: String
)

@Serializable
enum class AnalysisStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

@Serializable
enum class Verdict {
    TRUE,
    LIE,
    UNCERTAIN
}
