package application.liedetector.api

import application.liedetector.exceptions.UnauthorizedException
import application.liedetector.models.AnalysisRequest
import application.liedetector.models.ApiConstants
import application.liedetector.security.UserPrincipal
import application.liedetector.service.AnalysisService
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

class AnalysisApi(
    private val analysisService: AnalysisService,
) {

    @Serializable
    @Resource(ApiConstants.ENDPOINT_ANALYZE)
    class Analyze(val parent: ApiV1 = ApiV1())

    fun register(route: Route) {
        route.authenticate(ApiConstants.AUTH_CONFIG_NAME) {
            route.post<Analyze> {
                val principal = call.principal<UserPrincipal>() 
                    ?: throw UnauthorizedException()
                
                val request = call.receive<AnalysisRequest>()
                val result = analysisService.startAnalysis(principal.uid, request)

                call.respond(result)
            }
        }
    }
}
