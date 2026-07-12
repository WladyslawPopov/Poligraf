package application.liedetector.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatus(
    val status: String,
    val version: String = "1.0.0"
)

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
data class SubjectDto(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false
)
