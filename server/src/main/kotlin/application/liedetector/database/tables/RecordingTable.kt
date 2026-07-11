package application.liedetector.database.tables

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object RecordingTable : UUIDTable("recordings") {
    val userId = reference("user_id", UserTable)
    val subjectId = reference("subject_id", SubjectTable).nullable()
    
    val storagePath = varchar("storage_path", 512)
    val durationMs = integer("duration_ms")
    val fileSize = long("file_size")
    
    val isAudioDeleted = bool("is_audio_deleted").default(false)

    // Our "Golden Asset" - the mathematical graph
    val acousticFingerprint = jsonb<JsonObject>("acoustic_fingerprint", { it.toString() }, { Json.decodeFromString(it) })
    
    // AI-extracted metadata for analysis without original audio
    val aiTranscriptionMetadata = jsonb<JsonObject>("ai_transcription_metadata", { it.toString() }, { Json.decodeFromString(it) })

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
