package com.campuscompanion.api

import com.campuscompanion.api.config.AppConfig
import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.plugins.configureCors
import com.campuscompanion.api.plugins.configureRouting
import com.campuscompanion.api.plugins.configureSerialization
import com.campuscompanion.api.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main() {
    logger.info("Starting Campus Companion API on port {}", AppConfig.port)
    logger.info("Firebase project: {}", AppConfig.firebaseProjectId)
    logger.info("Auth enforcement enabled: {}", AppConfig.authEnabled)

    // Touch FirebaseConfig now so a missing-credentials warning shows up immediately at boot,
    // rather than only on the first request that needs Firestore/Auth.
    if (!FirebaseConfig.isReady) {
        logger.warn(FirebaseConfig.statusMessage)
    }

    embeddedServer(Netty, port = AppConfig.port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    configureSerialization()
    configureCors()
    configureStatusPages()
    configureRouting()
}
