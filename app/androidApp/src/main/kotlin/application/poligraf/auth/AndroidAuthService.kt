package application.poligraf.auth

import application.poligraf.engine.auth.AuthService
import application.poligraf.models.KmpResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
