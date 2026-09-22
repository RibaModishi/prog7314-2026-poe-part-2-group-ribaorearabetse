package com.campuscompanion.api.routes

import com.campuscompanion.api.config.AppConfig
import com.campuscompanion.api.config.FirebaseConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val firebaseReady: Boolean,
    val firebaseProjectId: String,
    val detail: String
)

/**
 * Unauthenticated health check. Useful for confirming the server booted correctly
 * (e.g. after deploying to Cloud Run / Render) and for a first Postman request
 * before any Firebase credentials are wired up.
 */
fun Route.healthRoutes() {
    get("/health") {
        val ready = FirebaseConfig.isReady
        call.respond(
            if (ready) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
            HealthResponse(
                status = if (ready) "ok" else "degraded",
                firebaseReady = ready,
                firebaseProjectId = AppConfig.firebaseProjectId,
                detail = FirebaseConfig.statusMessage
            )
        )
    }
}
