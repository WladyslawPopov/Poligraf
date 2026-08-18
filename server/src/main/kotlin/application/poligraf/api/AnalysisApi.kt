package application.poligraf.api

import application.poligraf.models.AnalysisRequest
import application.poligraf.models.ApiConstants
import application.poligraf.service.AnalysisService
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

object AnalysisResources {
    @Serializable
    @Resource(ApiConstants.ENDPOINT_ANALYZE)
    class Analyze(val parent: ApiV1 = ApiV1())
}

fun Route.analysisApi(analysisService: AnalysisService) {
    authenticate(ApiConstants.AUTH_CONFIG_NAME) {
        rateLimit(RateLimitName(ApiConstants.RATE_LIMIT_HEAVY)) {
            post<AnalysisResources.Analyze> {
                val principal = call.requirePrincipal()
                val request = call.receive<AnalysisRequest>()
                val result = analysisService.startAnalysis(principal.uid, request)

                call.respond(result)
            }
        }
    }
}
