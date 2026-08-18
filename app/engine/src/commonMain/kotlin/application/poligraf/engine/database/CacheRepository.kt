package application.poligraf.engine.database

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

interface CacheRepository {
    suspend fun <T> get(key: String, serializer: KSerializer<T>): T?
    suspend fun <T> put(key: String, data: T, expiredTs: Long, serializer: KSerializer<T>)
    fun <T> getFlow(key: String, serializer: KSerializer<T>): Flow<T?>
    suspend fun deleteById(key: String)
    suspend fun clearAll()
}
