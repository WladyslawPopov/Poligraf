package application.poligraf.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object AppConfigTable : UUIDTable("app_configs") {
    val configKey = varchar("config_key", 128).uniqueIndex() // e.g., "min_version_android"
    
    // Using JsonElement to allow strings, numbers, or full objects as values
    val value = jsonb<JsonElement>("config_value", { it.toString() }, { Json.decodeFromString(it) })
    
    val description = text("description").nullable() // For admins to know what this toggle does
    
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
