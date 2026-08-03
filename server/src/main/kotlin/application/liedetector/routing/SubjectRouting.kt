package application.liedetector.routing

import application.liedetector.database.repository.SubjectRepository
import application.liedetector.database.repository.UserRepository
import application.liedetector.models.ApiConstants
import application.liedetector.models.SubjectDto
import application.liedetector.security.UserPrincipal
import io.ktor.http.*
import java.util.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureSubjectRouting(
    userRepository: UserRepository,
    subjectRepository: SubjectRepository
) {
    authenticate(ApiConstants.AUTH_CONFIG_NAME) {
        route(ApiConstants.API_V1) {
            get("/subjects") {
                val principal = call.principal<UserPrincipal>() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized, "User not found in context"
                )
                val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
                val subjects = subjectRepository.getSubjectsByUser(internalUserId)
                call.respond(subjects)
            }

            post("/subject") {
                val principal = call.principal<UserPrincipal>() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "User not found in context"
                )
                
                val dto = call.receive<SubjectDto>()
                val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
                
                val subjectId = subjectRepository.createSubject(internalUserId, dto)
                
                call.respond(dto.copy(id = subjectId.toString()))
            }

            get("/subject/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val subject = subjectRepository.getSubject(UUID.fromString(id))
                
                if (subject != null) {
                    call.respond(subject)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
