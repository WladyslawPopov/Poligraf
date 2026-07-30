package application.liedetector

import application.liedetector.database.DatabaseFactory
import application.liedetector.database.repository.AnalysisRepositoryImpl
import application.liedetector.database.repository.UserRepositoryImpl
import application.liedetector.models.ApiConstants
import application.liedetector.routing.configureAnalysisRouting
import application.liedetector.security.FirebaseAdmin
import application.liedetector.security.firebase
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun main() {
    DatabaseFactory.init()
    FirebaseAdmin.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        })
    }

    install(Authentication) {
        firebase()
    }

    val userRepository = UserRepositoryImpl()
    val analysisRepository = AnalysisRepositoryImpl()
    
    routing {
        get(ApiConstants.ENDPOINT_STATUS) {
            call.respond(mapOf("status" to "LieDetector Server is Running"))
        }

        configureAnalysisRouting(userRepository, analysisRepository)
    }
}
