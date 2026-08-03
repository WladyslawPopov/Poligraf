package application.liedetector.routing

import application.liedetector.database.repository.AnalysisRepository
import application.liedetector.database.repository.UserRepository
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.AnalysisStatus
import application.liedetector.models.ApiConstants
import application.liedetector.security.UserPrincipal
import application.liedetector.uicore.widgets.UiWidget
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureAnalysisRouting(
    userRepository: UserRepository,
    analysisRepository: AnalysisRepository
) {
    authenticate(ApiConstants.AUTH_CONFIG_NAME) {
        route(ApiConstants.API_V1) {
            post(ApiConstants.ENDPOINT_ANALYZE) {
                val principal = call.principal<UserPrincipal>() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "User not found in context"
                )
                
                val request = call.receive<AnalysisRequest>()

                // 1. Synchronize the user
                val internalUserId = userRepository.getOrCreateUser(
                    principal.uid,
                    principal.email
                )

                // 2. Create an initial analysis entry
                val analysisId = analysisRepository.createInitialAnalysis(internalUserId, request)

                call.respond(
                    mapOf(
                        "analysis_id" to analysisId.toString(),
                        "status" to AnalysisStatus.PENDING.name
                    )
                )
            }
        }
    }
}
