package application.poligraf.engine.database.internal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import application.poligraf.database.PoligrafDatabase
import application.poligraf.engine.database.CacheRepository
import application.poligraf.engine.database.common.dbDispatcher
import application.poligraf.engine.utils.getMinutesRemainingUntil
import application.poligraf.engine.utils.jsonSerializer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlin.time.Duration.Companion.milliseconds

internal class CacheRepositoryImpl(private val db: PoligrafDatabase) : CacheRepository {

    override suspend fun <T> get(key: String, serializer: KSerializer<T>): T? = withContext(dbDispatcher) {
        try {
            val cached = db.cacheQueries.selectByRequestId(key).executeAsOneOrNull()

            if (cached != null && !isCacheExpired(cached.timestamp)) {
                try {
                    jsonSerializer.decodeFromString(serializer, cached.response)
                } catch (_: Throwable) {
                    null
                }
            } else {
                null
            }
        } catch (e: Throwable) {
            Napier.e(e) { "Error in CacheRepository.get for key '$key'" }
            null
        }
    }

    override suspend fun <T> put(key: String, data: T, expiredTs: Long, serializer: KSerializer<T>) {
        withContext(dbDispatcher) {
            try {
                val jsonResponse = jsonSerializer.encodeToString(serializer, data)

                db.cacheQueries.insertOrReplace(
                    requestId = key,
                    response = jsonResponse,
                    timestamp = expiredTs
                )
            } catch (e: Throwable) {
                Napier.e(e) { "Error in CacheRepository.put for key '$key'" }
            }
        }
    }

    override fun <T> getFlow(key: String, serializer: KSerializer<T>): Flow<T?> {
        return db.cacheQueries
            .selectByRequestId(key)
            .asFlow()
            .mapToOneOrNull(dbDispatcher)
            .retryWhen { _, attempt ->
                if (attempt < 3L) {
                    delay((100 * (attempt + 1)).milliseconds)
                    true
                } else {
                    false
                }
            }
            .map { cached ->
                if (cached != null && !isCacheExpired(cached.timestamp)) {
                    try {
                        jsonSerializer.decodeFromString(serializer, cached.response)
                    } catch (_: Throwable) {
                        null
                    }
                } else {
                    null
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun deleteById(key: String) {
        withContext(dbDispatcher) {
            try {
                db.cacheQueries.deleteByRequestId(key)
            } catch (e: Throwable) {
                Napier.e(e) { "Error deleting key '$key'" }
            }
        }
    }

    override suspend fun clearAll() {
        withContext(dbDispatcher) {
            try {
                db.cacheQueries.clearAll()
            } catch (e: Throwable) {
                Napier.e(e) { "Error clearing cache" }
            }
        }
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        return getMinutesRemainingUntil(timestamp) == null
    }
}
