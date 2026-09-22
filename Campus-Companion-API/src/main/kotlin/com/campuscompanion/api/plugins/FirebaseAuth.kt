package com.campuscompanion.api.plugins

import com.campuscompanion.api.config.AppConfig
import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.util.UnauthorizedException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("FirebaseAuth")

/**
 * Verifies the Firebase ID token sent by the Android client in the
 * `Authorization: Bearer <token>` header and returns the caller's Firebase UID.
 *
 * Every route that reads/writes user-specific data (profile, favourites) must
 * call this first, per the brief's requirement that "all API requests that
 * require user information [be] authenticated using the user's SSO token".
 *
 * For local development/testing without a real Firebase ID token, set
 * AUTH_ENABLED=false and send an `X-Debug-Uid: <any-string>` header instead -
 * this must stay OFF (default) for anything resembling a real deployment.
 */
fun ApplicationCall.requireUid(): String {
    if (!AppConfig.authEnabled) {
        val debugUid = request.header("X-Debug-Uid")
        if (debugUid != null && debugUid.isNotBlank()) {
            logger.warn("AUTH_ENABLED=false - accepting debug UID '{}'. Do not use this in production.", debugUid)
            return debugUid
        }
        throw UnauthorizedException("AUTH_ENABLED=false but no X-Debug-Uid header was supplied")
    }

    val header = request.header("Authorization")
        ?: throw UnauthorizedException("Missing Authorization header")

    if (!header.startsWith("Bearer ", ignoreCase = true)) {
        throw UnauthorizedException("Authorization header must be in the form 'Bearer <idToken>'")
    }

    val idToken = header.removePrefix("Bearer ").removePrefix("bearer ").trim()
    if (idToken.isEmpty()) {
        throw UnauthorizedException("Bearer token is empty")
    }

    return try {
        val decodedToken = FirebaseConfig.auth().verifyIdToken(idToken)
        decodedToken.uid
    } catch (ex: Exception) {
        logger.warn("Failed to verify Firebase ID token: {}", ex.message)
        throw UnauthorizedException("Invalid or expired authentication token")
    }
}
