package com.campuscompanion.api.plugins

import com.campuscompanion.api.models.ApiError
import com.campuscompanion.api.util.BadRequestException
import com.campuscompanion.api.util.ForbiddenException
import com.campuscompanion.api.util.NotFoundException
import com.campuscompanion.api.util.UnauthorizedException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

/**
 * Central error-handling: every exception thrown anywhere in a route ends up
 * here and is converted into a consistent JSON error shape with the right
 * HTTP status code, per the brief's "return consistent JSON error responses
 * such as 400, 401, 403, 404, and 500" requirement.
 */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("bad_request", cause.message ?: "Invalid request", 400)
            )
        }
        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("bad_request", "Malformed JSON body: ${cause.message}", 400)
            )
        }
        exception<UnauthorizedException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiError("unauthorized", cause.message ?: "Authentication required", 401)
            )
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiError("forbidden", cause.message ?: "Access denied", 403)
            )
        }
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiError("not_found", cause.message ?: "Resource not found", 404)
            )
        }
        exception<IllegalStateException> { call, cause ->
            // Thrown by FirebaseConfig when Firestore/Auth are unavailable (e.g. missing credentials)
            logger.error("Illegal state (likely Firebase not configured): {}", cause.message)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError("service_unavailable", cause.message ?: "A required service is not configured", 500)
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError("internal_server_error", "Something went wrong. Please try again later.", 500)
            )
        }
    }
}
