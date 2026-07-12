package application.liedetector.auth

import application.liedetector.engine.auth.AuthService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AndroidAuthService : AuthService {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously(): Result<Unit> {
        return try {
            auth.signInAnonymously().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIdToken(): String? {
        return auth.currentUser?.getIdToken(false)?.await()?.token
    }

    override fun isAuthorized(): Boolean {
        return auth.currentUser != null
    }
}
