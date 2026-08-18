package application.poligraf.api

import application.poligraf.exceptions.UnauthorizedException
import application.poligraf.security.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/v1")
class ApiV1

/**
 * Helper to safely retrieve the authenticated user principal from the call.
 * Throws [UnauthorizedException] if not found.
 */
fun ApplicationCall.requirePrincipal(): UserPrincipal =
    principal<UserPrincipal>() ?: throw UnauthorizedException("User not authenticated")
