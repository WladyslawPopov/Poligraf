package application.poligraf.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object FeedbackTable : UUIDTable("feedback") {
    val analysisId = reference("analysis_id", AnalysisTable)
    val wasActuallyTrue = bool("was_actually_true")
    val userConfidence = integer("user_confidence").default(100) // 0-100 score
    
    // Quick tags, error types, and any extended survey data
    val detailedFeedback = jsonb<JsonObject>("detailed_feedback", { it.toString() }, { Json.decodeFromString(it) })

    val comment = text("comment").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
