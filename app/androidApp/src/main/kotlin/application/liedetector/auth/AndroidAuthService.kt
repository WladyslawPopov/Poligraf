package application.liedetector.auth

import application.liedetector.engine.auth.AuthService
import application.liedetector.models.KmpResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AndroidAuthService : AuthService {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously(): KmpResult<Unit> {
        return try {
            auth.signInAnonymously().await()
            KmpResult.Success(Unit)
        } catch (e: Exception) {
            KmpResult.Error(e)
        }
    }

    override suspend fun getIdToken(): String? {
        return auth.currentUser?.getIdToken(false)?.await()?.token
    }

    override fun isAuthorized(): Boolean {
        return auth.currentUser != null
    }
}
