package com.campuscompanion.api.config

/**
 * Central place for environment-driven configuration.
 *
 * All values can be overridden with environment variables, which is how they
 * should be supplied in any hosted environment (Cloud Run, Render, Railway, etc.).
 * Sensible local-development defaults are provided so the API can be run with
 * zero configuration for a first smoke test (see README "Running locally").
 */
object AppConfig {

    /**
     * Port the Ktor server listens on. Defaults to 8090 rather than the more
     * conventional 8080, because 8080 is very commonly already taken on dev
     * machines (Docker Desktop, Tomcat, Jenkins, etc. all default to it).
     * Hosted platforms (Cloud Run, Render, Railway) always inject their own
     * $PORT value at deploy time, so this default only matters for local runs.
     */
    val port: Int
        get() = System.getenv("PORT")?.toIntOrNull() ?: 8090

    /**
     * Path to the Firebase service-account JSON credentials file.
     * Download this from Firebase Console > Project Settings > Service Accounts
     * for project [firebaseProjectId] and NEVER commit it to git.
     */
    val firebaseCredentialsPath: String
        get() = System.getenv("FIREBASE_CREDENTIALS_PATH")
            ?: System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
            ?: "firebase-service-account.json"

    /** Firebase project ID, used for logging/diagnostics only (not required by the SDK itself). */
    val firebaseProjectId: String
        get() = System.getenv("FIREBASE_PROJECT_ID") ?: "campus-companion-d4052"

    /** Comma-separated list of allowed CORS origins. "*" allows any origin (fine for coursework demos). */
    val allowedCorsOrigins: List<String>
        get() = (System.getenv("ALLOWED_CORS_ORIGINS") ?: "*")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    /**
     * When true, requests missing/invalid Firebase ID tokens are rejected with 401.
     * Kept as a flag (rather than hardcoded) so the team can temporarily disable auth
     * while wiring up the Android client, without touching route code.
     * Defaults to true - auth should stay ON for anything beyond local debugging.
     */
    val authEnabled: Boolean
        get() = System.getenv("AUTH_ENABLED")?.toBooleanStrictOrNull() ?: true

    /**
     * OpenWeather API key (https://openweathermap.org/api), used by
     * GET /api/recommendations/weather. If unset, that endpoint falls back to a
     * clearly-labelled mock response so the route can still be demoed/tested
     * before a real key is obtained.
     */
    val openWeatherApiKey: String?
        get() = System.getenv("OPENWEATHER_API_KEY")?.takeIf { it.isNotBlank() }

    /** Default campus coordinates used for the weather lookup; override per real campus. */
    val campusLatitude: Double
        get() = System.getenv("CAMPUS_LATITUDE")?.toDoubleOrNull() ?: -29.8587

    val campusLongitude: Double
        get() = System.getenv("CAMPUS_LONGITUDE")?.toDoubleOrNull() ?: 31.0218
}
