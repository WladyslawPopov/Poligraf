package application.poligraf

import application.poligraf.api.*
import application.poligraf.database.DatabaseFactory
import application.poligraf.database.repository.*
import application.poligraf.di.appModule
import application.poligraf.exceptions.*
import application.poligraf.models.ApiConstants
import application.poligraf.models.ApiErrorResponse
import application.poligraf.security.FirebaseAdmin
import application.poligraf.security.firebase
import application.poligraf.security.UserPrincipal
import application.poligraf.service.AnalysisService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun main() {
    DatabaseFactory.init()
    FirebaseAdmin.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        })
    }

    install(Resources)

    // Security: Payload size limit (e.g., 5MB)
    intercept(ApplicationCallPipeline.Plugins) {
        val contentLength = call.request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        if (contentLength != null && contentLength > 5 * 1024 * 1024) {
            call.respond(HttpStatusCode.PayloadTooLarge, "Request size exceeds limit (5MB)")
            finish()
        }
    }

    // Security: Rate Limiting
    install(RateLimit) {
        global {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
            requestKey { call ->
                val uid = call.principal<UserPrincipal>()?.uid 
                uid ?: call.request.headers["X-Device-ID"] ?: call.request.local.remoteHost
            }
        }
        register(RateLimitName(ApiConstants.RATE_LIMIT_HEAVY)) {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
            requestKey { call ->
                val uid = call.principal<UserPrincipal>()?.uid 
                uid ?: call.request.headers["X-Device-ID"] ?: call.request.local.remoteHost
            }
        }
    }

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val status = when (cause) {
                is UserNotFoundException, is SubjectNotFoundException -> HttpStatusCode.NotFound
                is UnauthorizedException -> HttpStatusCode.Unauthorized
                is ForbiddenException -> HttpStatusCode.Forbidden
                is InvalidRequestException -> HttpStatusCode.BadRequest
            }
            call.respond(
                status,
                ApiErrorResponse(
                    message = cause.message ?: "An error occurred",
                    code = cause.code
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorResponse(
                    message = cause.message ?: "An unexpected error occurred",
                    code = "INTERNAL_SERVER_ERROR"
                )
            )
        }
        
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(
                status,
                ApiErrorResponse(
                    message = "Unauthorized access",
                    code = "UNAUTHORIZED"
                )
            )
        }
    }

    install(Authentication) {
        firebase()
    }

    // Dependencies injected via Koin
    val userRepository by inject<UserRepository>()
    val subjectRepository by inject<SubjectRepository>()
    val analysisService by inject<AnalysisService>()
    
    routing {
        statusApi()
        userApi(userRepository)
        subjectApi(userRepository, subjectRepository)
        analysisApi(analysisService)
    }
}
