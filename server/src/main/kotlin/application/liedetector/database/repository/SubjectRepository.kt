package application.liedetector.database.repository

import application.liedetector.database.DatabaseFactory.dbQuery
import application.liedetector.database.tables.SubjectTable
import application.liedetector.models.SubjectDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import java.util.*

interface SubjectRepository {
    suspend fun createSubject(userId: UUID, dto: SubjectDto): SubjectDto
    suspend fun getSubject(id: UUID, userId: UUID): SubjectDto?
    suspend fun getSubjectsByUser(userId: UUID): List<SubjectDto>
    suspend fun deleteSubjects(userId: UUID, ids: List<UUID>): Boolean
}

class SubjectRepositoryImpl : SubjectRepository {
    private val emptyJson = Json.parseToJsonElement("{}").jsonObject

    override suspend fun createSubject(userId: UUID, dto: SubjectDto): SubjectDto = dbQuery {
        val count = SubjectTable.selectAll()
            .where { SubjectTable.ownerId eq userId }
            .count()
        
        val name = if (dto.name.trim().isBlank() || 
            dto.name.startsWith("Undefined") || 
            dto.name.startsWith("undefined")) {
            "Subject ${count + 1}"
        } else {
            dto.name.trim()
        }

        val id = SubjectTable.insertAndGetId {
            it[ownerId] = userId
            it[this.name] = name
            it[avatar] = dto.avatar
            it[isDefaultAvatar] = dto.isDefaultAvatar
            it[description] = dto.description
            it[isPublic] = dto.isPublic
            it[personalityConfig] = emptyJson
            it[stats] = emptyJson
            if (dto.metadata.isNotEmpty()) {
                it[additionalData] = Json.encodeToJsonElement(dto.metadata).jsonObject
            } else {
                it[additionalData] = emptyJson
            }
        }.value

        dto.copy(id = id.toString(), name = name)
    }

    override suspend fun getSubject(id: UUID, userId: UUID): SubjectDto? = dbQuery {
        SubjectTable.selectAll()
            .where { (SubjectTable.id eq id) and (SubjectTable.ownerId eq userId) }
            .singleOrNull()?.let { row ->
                val metadata: Map<String, String> = try {
                    Json.decodeFromJsonElement(row[SubjectTable.additionalData])
                } catch (e: Exception) {
                    emptyMap()
                }
                
                SubjectDto(
                    id = row[SubjectTable.id].value.toString(),
                    name = row[SubjectTable.name],
                    avatar = row[SubjectTable.avatar],
                    isDefaultAvatar = row[SubjectTable.isDefaultAvatar],
                    description = row[SubjectTable.description],
                    isPublic = row[SubjectTable.isPublic],
                    metadata = metadata
                )
            }
    }

    override suspend fun getSubjectsByUser(userId: UUID): List<SubjectDto> = dbQuery {
        SubjectTable.selectAll()
            .where { SubjectTable.ownerId eq userId }
            .orderBy(SubjectTable.updatedAt, org.jetbrains.exposed.v1.core.SortOrder.DESC)
            .map { row ->
                val metadata: Map<String, String> = try {
                    Json.decodeFromJsonElement(row[SubjectTable.additionalData])
                } catch (e: Exception) {
                    emptyMap()
                }

                SubjectDto(
                    id = row[SubjectTable.id].value.toString(),
                    name = row[SubjectTable.name],
                    avatar = row[SubjectTable.avatar],
                    isDefaultAvatar = row[SubjectTable.isDefaultAvatar],
                    description = row[SubjectTable.description],
                    isPublic = row[SubjectTable.isPublic],
                    metadata = metadata
                )
            }
    }

    override suspend fun deleteSubjects(userId: UUID, ids: List<UUID>): Boolean = dbQuery {
        SubjectTable.deleteWhere { (SubjectTable.ownerId eq userId) and (SubjectTable.id inList ids) } > 0
    }
}
