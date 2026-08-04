package application.liedetector.api

import application.liedetector.database.repository.SubjectRepository
import application.liedetector.database.repository.UserRepository
import application.liedetector.exceptions.UnauthorizedException
import application.liedetector.models.ApiConstants
import application.liedetector.models.SubjectDto
import application.liedetector.security.UserPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.get
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import java.util.*

class SubjectApi(
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository
) {

    @Serializable
    @Resource(ApiConstants.ENDPOINT_SUBJECTS)
    class Subjects(val parent: ApiV1 = ApiV1()) {
        @Serializable
        @Resource("{id}")
        class Id(val parent: Subjects, val id: String)
    }

    fun register(route: Route) {
        route.authenticate(ApiConstants.AUTH_CONFIG_NAME) {
            // GET /api/v1/subjects
            route.get<Subjects> {
                val principal = call.principal<UserPrincipal>() 
                    ?: throw UnauthorizedException()
                
                val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
                val subjects = subjectRepository.getSubjectsByUser(internalUserId)
                call.respond(subjects)
            }

            // POST /api/v1/subjects
            route.post<Subjects> {
                val principal = call.principal<UserPrincipal>() 
                    ?: throw UnauthorizedException()
                
                val dto = call.receive<SubjectDto>()
                val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
                
                val subjectId = subjectRepository.createSubject(internalUserId, dto)
                call.respond(dto.copy(id = subjectId.toString()))
            }

            // GET /api/v1/subjects/{id}
            route.get<Subjects.Id> { resource ->
                val principal = call.principal<UserPrincipal>() 
                    ?: throw UnauthorizedException()
                
                val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
                val subject = subjectRepository.getSubject(UUID.fromString(resource.id), internalUserId)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                
                call.respond(subject)
            }
        }
    }
}
