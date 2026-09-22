package com.campuscompanion.api.routes

import com.campuscompanion.api.models.UpsertProfileRequest
import com.campuscompanion.api.plugins.requireUid
import com.campuscompanion.api.repositories.UserRepository
import com.campuscompanion.api.util.Validation
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

/**
 * POST /api/auth/profile - create/update the signed-in user's basic profile.
 * GET  /api/users/me     - fetch the signed-in user's profile.
 * PUT  /api/users/me/settings - update settings (currently: preferredLanguage).
 *
 * All three require a valid Firebase ID token (see FirebaseAuth.kt), matching
 * "All API requests that require user information will be authenticated using
 * the user's SSO token."
 */
fun Route.authRoutes(userRepository: UserRepository) {

    post("/api/auth/profile") {
        val uid = call.requireUid()
        val body = call.receive<UpsertProfileRequest>()

        Validation.requireNonBlank(body.name, "name")
        Validation.requireValidEmail(body.email)
        Validation.requireValidLanguage(body.preferredLanguage)

        val profile = userRepository.upsert(uid, body.name, body.email, body.preferredLanguage)
        call.respond(HttpStatusCode.OK, profile)
    }

    get("/api/users/me") {
        val uid = call.requireUid()
        val profile = userRepository.findByUid(uid)
            ?: throw com.campuscompanion.api.util.NotFoundException(
                "No profile found for this user yet. Call POST /api/auth/profile first."
            )
        call.respond(HttpStatusCode.OK, profile)
    }

    put("/api/users/me/settings") {
        val uid = call.requireUid()
        val body = call.receive<Map<String, String>>()
        val language = body["preferredLanguage"]
        language?.let { Validation.requireValidLanguage(it) }

        val profile = userRepository.updateSettings(uid, language)
        call.respond(HttpStatusCode.OK, profile)
    }
}
