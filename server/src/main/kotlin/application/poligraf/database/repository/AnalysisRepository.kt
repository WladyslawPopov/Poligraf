package application.poligraf.database.repository

import application.poligraf.database.DatabaseFactory.dbQuery
import application.poligraf.database.tables.AnalysisTable
import application.poligraf.database.tables.RecordingTable
import application.poligraf.models.AnalysisRequest
import application.poligraf.models.AnalysisStatus
import application.poligraf.models.Verdict
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

interface AnalysisRepository {
    suspend fun createInitialAnalysis(userId: UUID, request: AnalysisRequest): UUID
    suspend fun updateAnalysisResult(analysisId: UUID, verdict: Verdict, reasoning: String)
}

class AnalysisRepositoryImpl : AnalysisRepository {
    override suspend fun createInitialAnalysis(userId: UUID, request: AnalysisRequest): UUID = dbQuery {
        // 1. Register the recording
        val recordingId = RecordingTable.insertAndGetId {
            it[this.userId] = userId
            it[this.subjectId] = request.subjectId?.let { id -> UUID.fromString(id) }
            it[this.storagePath] = request.storagePath
            it[this.durationMs] = 0 // Will be updated after file processing
            it[this.fileSize] = 0L // Will be updated after file processing
            it[this.acousticFingerprint] = buildJsonObject { }
            it[this.aiTranscriptionMetadata] = buildJsonObject { }
        }

        // 2. Create an empty analysis entry
        AnalysisTable.insertAndGetId {
            it[this.userId] = userId
            it[this.recordingId] = recordingId
            it[this.verdict] = AnalysisStatus.PENDING.name
            it[this.reasoning] = "Analysis is in progress..."
            it[this.contextMetadata] = buildJsonObject {
                put("user_context", request.contextText)
            }
            it[this.detailedMetrics] = buildJsonObject { }
            it[this.rawAiResponse] = buildJsonObject { }
        }.value
    }

    override suspend fun updateAnalysisResult(analysisId: UUID, verdict: Verdict, reasoning: String): Unit = dbQuery {
        AnalysisTable.update({ AnalysisTable.id eq analysisId }) {
            it[this.verdict] = verdict.name
            it[this.reasoning] = reasoning
        }
        Unit
    }
}
