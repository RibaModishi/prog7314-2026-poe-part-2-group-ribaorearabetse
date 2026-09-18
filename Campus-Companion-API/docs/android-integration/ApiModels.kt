package com.campuscompanion.android.api

/**
 * Retrofit/Gson data models mirroring the Campus Companion API's JSON shapes 1:1.
 * Field names match the backend exactly (see docs/API_DOCUMENTATION.md) so no
 * @SerializedName annotations are needed.
 *
 * Copy this file into your Android app (e.g. app/src/main/java/.../api/ApiModels.kt)
 * and adjust the package name to match your project.
 */

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val preferredLanguage: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class UpsertProfileRequest(
    val name: String,
    val email: String,
    val preferredLanguage: String = "en"
)

data class UpdateSettingsRequest(
    val preferredLanguage: String? = null
)

data class CampusLocation(
    val id: String,
    val name: String,
    val category: String, // LIBRARY, STUDY_SPACE, LAB, CAFETERIA, PARKING, STUDENT_SUPPORT, LECTURE_VENUE, CHILL_SPOT
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val openingHours: String,
    val isIndoor: Boolean,
    val isCovered: Boolean,
    val hasAirConditioning: Boolean,
    val hasWifi: Boolean,
    val quietLevel: Int,
    val createdAt: Long
)

data class EventOrOpportunity(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // CAMPUS_EVENT, BURSARY, INTERNSHIP, WORKSHOP, SOCIETY_ACTIVITY
    val organiser: String,
    val startDateTime: String, // ISO-8601, e.g. "2026-10-05T16:00:00Z"
    val endDateTime: String?,
    val locationId: String?,
    val externalUrl: String?,
    val isOutdoor: Boolean,
    val tags: List<String>,
    val createdAt: Long
)

data class Favourite(
    val id: String,
    val userId: String,
    val itemType: String, // "LOCATION" or "EVENT"
    val itemId: String,
    val savedAt: Long
)

data class CreateFavouriteRequest(
    val itemType: String,
    val itemId: String
)

data class SyncAction(
    val action: String, // "SAVE" or "REMOVE"
    val itemType: String,
    val itemId: String,
    val clientTimestamp: Long
)

data class SyncRequest(
    val actions: List<SyncAction>
)

data class SyncResult(
    val itemType: String,
    val itemId: String,
    val action: String,
    val success: Boolean,
    val message: String?
)

data class SyncResponse(
    val syncResults: List<SyncResult>
)

data class WeatherRecommendation(
    val condition: String, // RAIN, HOT, COLD, WIND, CLEAR
    val temperatureCelsius: Double?,
    val message: String,
    val recommendedLocationIds: List<String>,
    val recommendedLocations: List<CampusLocation>
)

/** Shape of every error response - see docs/API_DOCUMENTATION.md for the full status code table. */
data class ApiError(
    val error: String,
    val message: String,
    val status: Int
)
