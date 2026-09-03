package application.poligraf.domain.history.repository

import application.poligraf.domain.history.model.Session
import application.poligraf.domain.history.model.SessionNote
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getSessions(): Flow<List<Session>>
    fun getSessionById(id: String): Flow<Session?>
    suspend fun updateSessionMetadata(id: String, title: String, notes: String)
    suspend fun deleteSession(id: String)
    suspend fun getSessionCount(): Long
    fun getNotesForSession(sessionId: String): Flow<List<SessionNote>>
    suspend fun addNote(sessionId: String, timestamp: Long, text: String, markerColor: String? = null, markerShape: String? = null)
    suspend fun deleteNote(noteId: String)
}
