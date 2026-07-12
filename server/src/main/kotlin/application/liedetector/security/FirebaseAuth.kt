package application.liedetector.security

import application.liedetector.models.ApiConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

class UserPrincipal(val uid: String, val email: String)

fun AuthenticationConfig.firebase(name: String = ApiConstants.AUTH_CONFIG_NAME) {
    val provider = FirebaseAuthenticationProvider(Config(name))
    register(provider)
}

class Config(name: String?) : AuthenticationProvider.Config(name)

class FirebaseAuthenticationProvider(config: Config) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val authHeader = call.request.headers[ApiConstants.HEADER_AUTHORIZATION]

        if (authHeader == null || !authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
            context.challenge("FirebaseToken", AuthenticationFailedCause.NoCredentials) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized, "Missing or invalid token")
                challenge.complete()
            }
            return
        }

        val token = authHeader.removePrefix(ApiConstants.BEARER_PREFIX)

        try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
            context.principal(UserPrincipal(decodedToken.uid, decodedToken.email ?: ""))
        } catch (e: FirebaseAuthException) {
            context.challenge("FirebaseToken", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized, "Token verification failed: ${e.message}")
                challenge.complete()
            }
        } catch (e: Exception) {
            context.error("AuthError", AuthenticationFailedCause.Error(e.message ?: "Unknown error"))
        }
    }
}
