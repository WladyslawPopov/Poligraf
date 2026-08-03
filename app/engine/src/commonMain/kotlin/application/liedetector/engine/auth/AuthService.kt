package application.liedetector.engine.auth

import application.liedetector.models.KmpResult

interface AuthService {
    suspend fun signInAnonymously(): KmpResult<Unit>
    suspend fun getIdToken(): String?
    fun isAuthorized(): Boolean
}
