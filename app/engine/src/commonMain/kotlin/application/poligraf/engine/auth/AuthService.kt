package application.poligraf.engine.auth

interface AuthService {
    suspend fun signInAnonymously()
    suspend fun getIdToken(): String?
    fun isAuthorized(): Boolean
}
