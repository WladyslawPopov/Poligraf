package application.poligraf.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object TransactionTable : UUIDTable("transactions") {
    val userId = reference("user_id", UserTable)
    val analysisId = reference("analysis_id", AnalysisTable).nullable() // Link to analysis if tokens were spent
    
    val tokensDelta = long("tokens_delta") // +500 for purchase, -10 for analysis
    
    val type = varchar("type", 32) // PURCHASE, SPEND, REFUND, BONUS
    val status = varchar("status", 32) // COMPLETED, PENDING, FAILED
    
    val metadata = jsonb<JsonObject>("metadata", { it.toString() }, { Json.decodeFromString(it) })
    
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
