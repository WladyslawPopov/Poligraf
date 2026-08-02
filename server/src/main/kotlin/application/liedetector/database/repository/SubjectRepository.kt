package application.liedetector.database.repository

import application.liedetector.database.DatabaseFactory.dbQuery
import application.liedetector.database.tables.SubjectTable
import application.liedetector.models.SubjectDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import java.util.*

interface SubjectRepository {
    suspend fun createSubject(userId: UUID, dto: SubjectDto): UUID
}

class SubjectRepositoryImpl : SubjectRepository {
    override suspend fun createSubject(userId: UUID, dto: SubjectDto): UUID = dbQuery {
        SubjectTable.insertAndGetId {
            it[ownerId] = userId
            it[name] = dto.name
            it[avatar] = dto.avatar
            it[isDefaultAvatar] = dto.isDefaultAvatar
            it[description] = dto.description
            it[isPublic] = dto.isPublic
            if (dto.metadata.isNotEmpty()) {
                it[additionalData] = Json.encodeToJsonElement(dto.metadata).jsonObject
            }
        }.value
    }
}
