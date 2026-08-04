package application.liedetector.api

import application.liedetector.database.repository.UserRepository
import application.liedetector.exceptions.UnauthorizedException
import application.liedetector.models.ApiConstants
import application.liedetector.models.UserDto
import application.liedetector.security.UserPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

class UserApi(private val userRepository: UserRepository) {

    @Serializable
    @Resource(ApiConstants.ENDPOINT_USER_SYNC)
    class Sync(val parent: ApiV1 = ApiV1())

    @Serializable
    @Resource("profile")
    class Profile(val parent: ApiV1 = ApiV1())

    fun register(route: Route) {
        route.authenticate(ApiConstants.AUTH_CONFIG_NAME) {
            route.post<Sync> {
                val principal = call.principal<UserPrincipal>() 
                    ?: throw UnauthorizedException("User not found in context")
                
                val userDto = call.receive<UserDto>()
                val userId = userRepository.syncUser(principal.uid, userDto)
                
                call.respond(HttpStatusCode.OK, mapOf("user_id" to userId.toString()))
            }
        }
    }
}
