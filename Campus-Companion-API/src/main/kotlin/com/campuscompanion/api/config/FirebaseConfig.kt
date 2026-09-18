package com.campuscompanion.api.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

/**
 * Lazily initialises the Firebase Admin SDK (Firestore + Auth) from the
 * service-account JSON described in AppConfig.firebaseCredentialsPath.
 *
 * Initialisation is lazy and safe to call multiple times: the server can still
 * boot (and answer /health) even if credentials are missing, so a teammate
 * pulling the repo for the first time can run it before wiring up Firebase.
 * Any route that actually touches Firestore.() or Auth.() will throw a clear
 * IllegalStateException, which StatusPagesConfig turns into a helpful 500 response.
 */
object FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    private var initialised = false
    private var initError: String? = null

    private val app: FirebaseApp? by lazy { initialiseIfNeeded() }

    val isReady: Boolean
        get() {
            app
            return initialised
        }

    val statusMessage: String
        get() = initError ?: "Firebase Admin SDK initialised for project '${AppConfig.firebaseProjectId}'"

    /** Firestore database handle. Throws if Firebase could not be initialised. */
    fun firestore(): Firestore {
        val initialisedApp = app
            ?: throw IllegalStateException(
                "Firestore is not available: $initError. " +
                    "Place a valid firebase-service-account.json in the project root " +
                    "(see firebase-service-account.example.json) or set FIREBASE_CREDENTIALS_PATH."
            )
        return FirestoreClient.getFirestore(initialisedApp)
    }

    /** Firebase Auth handle, used to verify ID tokens sent by the Android client. */
    fun auth(): FirebaseAuth {
        val initialisedApp = app
            ?: throw IllegalStateException(
                "Firebase Auth is not available: $initError. " +
                    "Place a valid firebase-service-account.json in the project root " +
                    "(see firebase-service-account.example.json) or set FIREBASE_CREDENTIALS_PATH."
            )
        return FirebaseAuth.getInstance(initialisedApp)
    }

    private fun initialiseIfNeeded(): FirebaseApp? {
        val credentialsPath = AppConfig.firebaseCredentialsPath
        val file = File(credentialsPath)

        if (!file.exists()) {
            initError = "Credentials file not found at '$credentialsPath'"
            logger.warn(
                "Firebase credentials not found at '{}'. The server will still start, " +
                    "but any endpoint touching Firestore/Auth will return 500 until this is fixed.",
                credentialsPath
            )
            return null
        }

        return try {
            FileInputStream(file).use { stream ->
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .setProjectId(AppConfig.firebaseProjectId)
                    .build()

                val firebaseApp = if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                } else {
                    FirebaseApp.getInstance()
                }
                initialised = true
                logger.info("Firebase Admin SDK initialised successfully for project '{}'", AppConfig.firebaseProjectId)
                firebaseApp
            }
        } catch (ex: Exception) {
            initError = "Failed to initialise Firebase Admin SDK: ${ex.message}"
            logger.error(initError, ex)
            null
        }
    }
}
