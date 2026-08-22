package application.poligraf.domain.repository

import application.poligraf.domain.model.User
import application.poligraf.domain.model.SessionRecord
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    
    fun getSessions(): Flow<List<SessionRecord>>
    suspend fun saveSession(session: SessionRecord)
    suspend fun deleteSession(id: String)
    suspend fun clearHistory()
}
