package application.liedetector.engine.auth

import application.liedetector.models.KmpResult

interface AuthService {
    /**
     * Performs anonymous sign-in to Firebase.
     */
    suspend fun signInAnonymously(): KmpResult<Unit>

    /**
     * Returns the current ID token for server requests.
     */
    suspend fun getIdToken(): String?

    /**
     * Checks if the user is authorized.
     */
    fun isAuthorized(): Boolean
}
