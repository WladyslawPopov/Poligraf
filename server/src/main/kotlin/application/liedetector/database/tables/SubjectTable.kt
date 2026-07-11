package application.liedetector.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object SubjectTable : UUIDTable("subjects") {
    val ownerId = reference("owner_id", UserTable).nullable() // Nullable for global public subjects
    val isPublic = bool("is_public").default(false)
    val name = varchar("name", 255)
    val photoUrl = varchar("photo_url", 512).nullable()
    val description = text("description").nullable()

    // Personality & Acoustic Config (Temperament, speech patterns, public presets)
    val personalityConfig = jsonb<JsonObject>("personality_config", { it.toString() }, { Json.decodeFromString(it) })

    // Aggregated stats (Total checks, truth ratio, trending score)
    val stats = jsonb<JsonObject>("stats", { it.toString() }, { Json.decodeFromString(it) })

    // Extensibility
    val additionalData = jsonb<JsonObject>("additional_data", { it.toString() }, { Json.decodeFromString(it) })

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
