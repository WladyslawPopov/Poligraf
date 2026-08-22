package application.poligraf.data.repository

import application.poligraf.database.PoligrafDatabase
import application.poligraf.domain.model.User
import application.poligraf.domain.model.SessionRecord
import application.poligraf.domain.repository.UserRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class UserRepositoryImpl(
    private val db: PoligrafDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : UserRepository {

    private val queries = db.appDatabaseQueries

    override fun getUser(): Flow<User?> {
        return queries.getUser()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.jsonData?.let { json.decodeFromString<User>(it) }
            }
    }

    override suspend fun saveUser(user: User) {
        val jsonStr = json.encodeToString(user)
        queries.upsertUser(user.id, jsonStr)
    }

    override fun getSessions(): Flow<List<SessionRecord>> {
        return queries.getAllSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { entity -> json.decodeFromString<SessionRecord>(entity.jsonData) }
            }
    }

    override suspend fun saveSession(session: SessionRecord) {
        val jsonStr = json.encodeToString(session)
        queries.insertSession(session.id, session.timestampEpochMillis, jsonStr)
    }

    override suspend fun deleteSession(id: String) {
        queries.deleteSessionById(id)
    }

    override suspend fun clearHistory() {
        queries.clearSessions()
    }
}
