package application.liedetector.data.subject

import application.liedetector.data.mapper.toDomain
import application.liedetector.data.subject.remote.SubjectRemoteDataSource
import application.liedetector.domain.model.Subject
import application.liedetector.engine.database.CacheRepository
import application.liedetector.engine.error.toAppException
import application.liedetector.engine.utils.nowAsEpochSeconds
import application.liedetector.models.KmpResult
import application.liedetector.models.SubjectDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

interface SubjectRepository {
    suspend fun createSubject(
        name: String, 
        avatar: String? = null,
        isDefaultAvatar: Boolean = true,
        description: String? = null
    ): KmpResult<Subject>

    suspend fun getSubject(id: String): KmpResult<Subject>
    fun getSubjects(): Flow<List<Subject>>
    suspend fun syncSubjects(): KmpResult<Unit>
}

class SubjectRepositoryImpl(
    private val remote: SubjectRemoteDataSource,
    private val cache: CacheRepository
) : SubjectRepository {

    override suspend fun createSubject(
        name: String, 
        avatar: String?,
        isDefaultAvatar: Boolean,
        description: String?
    ): KmpResult<Subject> {
        return try {
            val result = remote.createSubject(
                SubjectDto(
                    name = name, 
                    avatar = avatar,
                    isDefaultAvatar = isDefaultAvatar,
                    description = description
                )
            )
            KmpResult.Success(result.toDomain())
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }

    override suspend fun getSubject(id: String): KmpResult<Subject> {
        return try {
            KmpResult.Success(remote.getSubject(id).toDomain())
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }

    override fun getSubjects(): Flow<List<Subject>> {
        val cacheKey = "subjects_list"
        val serializer = ListSerializer(SubjectDto.serializer())
        
        return cache.getFlow(cacheKey, serializer).map { cached ->
            cached?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun syncSubjects(): KmpResult<Unit> {
        return try {
            val remoteSubjects = remote.getSubjects()
            cache.put("subjects_list", remoteSubjects, nowAsEpochSeconds() + 3600, ListSerializer(SubjectDto.serializer()))
            KmpResult.Success(Unit)
        } catch (e: Throwable) {
            KmpResult.Error(e.toAppException())
        }
    }
}
