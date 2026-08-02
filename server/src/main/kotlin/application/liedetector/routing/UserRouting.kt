package application.liedetector.routing

import application.liedetector.database.repository.UserRepository
import application.liedetector.models.ApiConstants
import application.liedetector.models.UserDto
import application.liedetector.security.UserPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureUserRouting(userRepository: UserRepository) {
    authenticate(ApiConstants.AUTH_CONFIG_NAME) {
        route(ApiConstants.API_V1) {
            post(ApiConstants.ENDPOINT_USER_SYNC) {
                val principal = call.principal<UserPrincipal>() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "User not found in context"
                )
                
                val userDto = call.receive<UserDto>()
                val userId = userRepository.syncUser(principal.uid, userDto)
                
                call.respond(HttpStatusCode.OK, mapOf("user_id" to userId.toString()))
            }
        }
    }
}
