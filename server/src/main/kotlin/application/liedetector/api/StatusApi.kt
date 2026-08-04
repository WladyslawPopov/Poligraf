package application.liedetector.api

import application.liedetector.models.ApiConstants
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource(ApiConstants.ENDPOINT_STATUS)
class StatusResource(val parent: ApiV1 = ApiV1())

fun Route.statusApi() {
    get<StatusResource> {
        call.respond(mapOf("status" to "LieDetector Server is Running"))
    }
}
