package application.poligraf.domain.repository

import application.poligraf.domain.model.Session
import application.poligraf.domain.model.SessionNote
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getSessions(): Flow<List<Session>>
    fun getSessionById(id: String): Flow<Session?>
    suspend fun updateSessionMetadata(id: String, title: String, notes: String)
    suspend fun deleteSession(id: String)
    suspend fun getSessionCount(): Long
    
    // Notes
    fun getNotesForSession(sessionId: String): Flow<List<SessionNote>>
    suspend fun addNote(sessionId: String, timestamp: Long, text: String, markerColor: String?, markerShape: String?)
    suspend fun deleteNote(noteId: String)
}
