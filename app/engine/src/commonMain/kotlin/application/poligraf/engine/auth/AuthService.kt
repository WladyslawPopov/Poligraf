package application.poligraf.engine.auth

import application.poligraf.models.KmpResult

interface AuthService {
    suspend fun signInAnonymously(): KmpResult<Unit>
    suspend fun getIdToken(): String?
    fun isAuthorized(): Boolean
}
