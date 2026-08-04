package application.liedetector.database.repository

import application.liedetector.database.DatabaseFactory.dbQuery
import application.liedetector.database.tables.SubjectTable
import application.liedetector.models.SubjectDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import java.util.*

interface SubjectRepository {
    suspend fun createSubject(userId: UUID, dto: SubjectDto): UUID
    suspend fun getSubject(id: UUID): SubjectDto?
    suspend fun getSubjectsByUser(userId: UUID): List<SubjectDto>
}

class SubjectRepositoryImpl : SubjectRepository {
    private val emptyJson = Json.parseToJsonElement("{}").jsonObject

    override suspend fun createSubject(userId: UUID, dto: SubjectDto): UUID = dbQuery {
        val count = SubjectTable.selectAll()
            .where { SubjectTable.ownerId eq userId }
            .count()
        
        val name = if (dto.name == "Undefined-1") "Undefined-${count + 1}" else dto.name

        SubjectTable.insertAndGetId {
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
    }

    override suspend fun getSubject(id: UUID): SubjectDto? = dbQuery {
        SubjectTable.selectAll()
            .where { SubjectTable.id eq id }
            .singleOrNull()?.let {
                SubjectDto(
                    id = it[SubjectTable.id].value.toString(),
                    name = it[SubjectTable.name],
                    avatar = it[SubjectTable.avatar],
                    isDefaultAvatar = it[SubjectTable.isDefaultAvatar],
                    description = it[SubjectTable.description],
                    isPublic = it[SubjectTable.isPublic],
                    metadata = emptyMap()
                )
            }
    }

    override suspend fun getSubjectsByUser(userId: UUID): List<SubjectDto> = dbQuery {
        SubjectTable.selectAll()
            .where { SubjectTable.ownerId eq userId }
            .orderBy(SubjectTable.updatedAt, org.jetbrains.exposed.v1.core.SortOrder.DESC)
            .map { 
                SubjectDto(
                    id = it[SubjectTable.id].value.toString(),
                    name = it[SubjectTable.name],
                    avatar = it[SubjectTable.avatar],
                    isDefaultAvatar = it[SubjectTable.isDefaultAvatar],
                    description = it[SubjectTable.description],
                    isPublic = it[SubjectTable.isPublic],
                    metadata = emptyMap()
                )
            }
    }
}
