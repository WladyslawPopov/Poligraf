package application.liedetector.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object SystemPromptTable : UUIDTable("system_prompts") {
    val promptKey = varchar("prompt_key", 64) // e.g., "CORE_ANALYSIS"
    val planTier = varchar("plan_tier", 32).default("FREE") // FREE, PRO, BUSINESS
    
    val content = text("content") // The instruction for Gemini
    val isActive = bool("is_active").default(true)
    
    val metadata = jsonb<JsonObject>("metadata", { it.toString() }, { Json.decodeFromString(it) })
    
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex("idx_unique_prompt_plan", promptKey, planTier)
    }
}
