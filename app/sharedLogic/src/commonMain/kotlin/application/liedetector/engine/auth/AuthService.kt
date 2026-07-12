package application.liedetector.engine.auth

interface AuthService {
    /**
     * Performs anonymous sign-in to Firebase.
     */
    suspend fun signInAnonymously(): Result<Unit>

    /**
     * Returns the current ID token for server requests.
     */
    suspend fun getIdToken(): String?

    /**
     * Checks if the user is authorized.
     */
    fun isAuthorized(): Boolean
}
