package application.poligraf.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object AnalysisTable : UUIDTable("analyses") {
    val userId = reference("user_id", UserTable)
    val recordingId = reference("recording_id", RecordingTable) // Ссылка на запись
    
    val verdict = varchar("verdict", 32) // Short human-readable verdict
    
    // Core Scoring Metrics (0-100)
    val deceptionProbability = integer("deception_probability").default(0)
    val acousticStressScore = integer("acoustic_stress_score").default(0)
    val manipulationScore = integer("manipulation_score").default(0)
    val logicConsistencyScore = integer("logic_consistency_score").default(0)
    
    val reasoning = text("reasoning")
    
    // Situation tags (#money, #romance)
    val contextMetadata = jsonb<JsonObject>("context_metadata", { it.toString() }, { Json.decodeFromString(it) })
    
    // Detailed fine-grained metrics
    val detailedMetrics = jsonb<JsonObject>("detailed_metrics", { it.toString() }, { Json.decodeFromString(it) })
    
    val rawAiResponse = jsonb<JsonObject>("raw_ai_response", { it.toString() }, { Json.decodeFromString(it) })
    
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
