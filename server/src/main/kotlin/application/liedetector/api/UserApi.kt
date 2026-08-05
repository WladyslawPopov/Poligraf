package application.liedetector.api

import application.liedetector.database.repository.UserRepository
import application.liedetector.models.ApiConstants
import application.liedetector.models.UserDto
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

object UserResources {
    @Serializable
    @Resource(ApiConstants.ENDPOINT_USER_SYNC)
    class Sync(val parent: ApiV1 = ApiV1())
}

fun Route.userApi(userRepository: UserRepository) {
    authenticate(ApiConstants.AUTH_CONFIG_NAME) {
        rateLimit(RateLimitName(ApiConstants.RATE_LIMIT_HEAVY)) {
            post<UserResources.Sync> {
                val principal = call.requirePrincipal()
                val userDto = call.receive<UserDto>()
                val userId = userRepository.syncUser(principal.uid, userDto)
                
                call.respond(HttpStatusCode.OK, mapOf("user_id" to userId.toString()))
            }
        }
    }
}
