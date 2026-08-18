package application.poligraf.auth

import application.poligraf.engine.auth.AuthService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AndroidAuthService : AuthService {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously() {
        try {
            auth.signInAnonymously().await()
        } catch (e: Exception) { }
    }

    override suspend fun getIdToken(): String? {
        return auth.currentUser?.getIdToken(false)?.await()?.token
    }

    override fun isAuthorized(): Boolean {
        return auth.currentUser != null
    }
}
