package application.poligraf.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import application.poligraf.database.PoligrafDatabase
import application.poligraf.domain.model.Session
import application.poligraf.domain.model.SessionNote
import application.poligraf.domain.repository.HistoryRepository
import application.poligraf.engine.database.common.dbDispatcher
import application.poligraf.engine.utils.nowAsEpochMilliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class HistoryRepositoryImpl(
    private val db: PoligrafDatabase
) : HistoryRepository {

    override fun getSessions(): Flow<List<Session>> {
        return db.appDatabaseQueries
            .getAllSessions()
            .asFlow()
            .mapToList(dbDispatcher)
            .map { list ->
                list.map { item ->
                    Session(
                        id = item.id,
                        timestamp = item.timestamp,
                        title = item.title ?: "",
                        notes = item.notes ?: "",
                        duration = item.duration,
                        isCompleted = item.isCompleted,
                        anomalyCount = item.anomalyCount.toInt()
                    )
                }
            }
    }

    override fun getSessionById(id: String): Flow<Session?> {
        return db.appDatabaseQueries
            .getSessionById(id)
            .asFlow()
            .mapToOneOrNull(dbDispatcher)
            .map { item ->
                item?.let {
                    Session(
                        id = it.id,
                        timestamp = it.timestamp,
                        title = it.title ?: "",
                        notes = it.notes ?: "",
                        duration = it.duration,
                        isCompleted = it.isCompleted,
                        anomalyCount = 0
                    )
                }
            }
    }

    override suspend fun updateSessionMetadata(id: String, title: String, notes: String) {
        withContext(dbDispatcher) {
            db.appDatabaseQueries.updateSessionMetadata(
                title = title,
                notes = notes,
                id = id
            )
        }
    }

    override suspend fun deleteSession(id: String) {
        withContext(dbDispatcher) {
            db.transaction {
                db.appDatabaseQueries.deleteFramesBySessionId(id)
                db.appDatabaseQueries.deleteSessionById(id)
            }
        }
    }

    override suspend fun getSessionCount(): Long {
        return withContext(dbDispatcher) {
            db.appDatabaseQueries.countSessions().executeAsOne()
        }
    }

    override fun getNotesForSession(sessionId: String): Flow<List<SessionNote>> {
        return db.appDatabaseQueries
            .getNotesBySessionId(sessionId)
            .asFlow()
            .mapToList(dbDispatcher)
            .map { list ->
                list.map {
                    SessionNote(
                        id = it.id,
                        sessionId = it.sessionId,
                        timestamp = it.timestamp,
                        text = it.text,
                        markerColor = it.markerColor,
                        markerShape = it.markerShape
                    )
                }
            }
    }

    override suspend fun addNote(sessionId: String, timestamp: Long, text: String, markerColor: String?, markerShape: String?) {
        withContext(dbDispatcher) {
            db.appDatabaseQueries.insertNote(
                id = "note_${nowAsEpochMilliseconds()}",
                sessionId = sessionId,
                timestamp = timestamp,
                text = text,
                markerColor = markerColor,
                markerShape = markerShape
            )
        }
    }

    override suspend fun deleteNote(noteId: String) {
        withContext(dbDispatcher) {
            db.appDatabaseQueries.deleteNoteById(noteId)
        }
    }
}
