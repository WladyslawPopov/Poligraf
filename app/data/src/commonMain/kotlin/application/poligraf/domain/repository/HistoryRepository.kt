package application.poligraf.domain.repository

import application.poligraf.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getSessions(): Flow<List<Session>>
    fun getSessionById(id: String): Flow<Session?>
    suspend fun updateSessionMetadata(id: String, title: String, notes: String)
    suspend fun deleteSession(id: String)
}
