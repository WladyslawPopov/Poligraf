package application.liedetector.api

import application.liedetector.database.repository.SubjectRepository
import application.liedetector.database.repository.UserRepository
import application.liedetector.models.ApiConstants
import application.liedetector.models.SubjectDto
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.resources.get
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import java.util.*

object SubjectResources {
    @Serializable
    @Resource(ApiConstants.ENDPOINT_SUBJECTS)
    class Subjects(val parent: ApiV1 = ApiV1()) {
        @Serializable
        @Resource("{id}")
        class Id(val parent: Subjects, val id: String)
    }
}

fun Route.subjectApi(
    userRepository: UserRepository,
    subjectRepository: SubjectRepository
) {
    authenticate(ApiConstants.AUTH_CONFIG_NAME) {
        // GET /api/v1/subjects
        get<SubjectResources.Subjects> {
            val principal = call.requirePrincipal()
            val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
            val subjects = subjectRepository.getSubjectsByUser(internalUserId)
            call.respond(subjects)
        }

        // POST /api/v1/subjects
        post<SubjectResources.Subjects> {
            val principal = call.requirePrincipal()
            val dto = call.receive<SubjectDto>()
            val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
            
            val subjectId = subjectRepository.createSubject(internalUserId, dto)
            call.respond(dto.copy(id = subjectId.toString()))
        }

        // GET /api/v1/subjects/{id}
        get<SubjectResources.Subjects.Id> { resource ->
            val principal = call.requirePrincipal()
            val internalUserId = userRepository.getOrCreateUser(principal.uid, principal.email)
            val subject = subjectRepository.getSubject(UUID.fromString(resource.id), internalUserId)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            
            call.respond(subject)
        }
    }
}
