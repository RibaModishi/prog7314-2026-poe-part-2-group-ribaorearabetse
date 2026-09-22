package com.example.campus_companion_prog.api

/**
 * Retrofit/Gson data models mirroring the Campus Companion API's JSON shapes 1:1.
 * Field names match the backend exactly (see docs/API_DOCUMENTATION.md).
 *
 * Source: docs/android-integration/ApiModels.kt (Khumo handover kit).
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
    val category: String,
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
    val type: String,
    val organiser: String,
    val startDateTime: String,
    val endDateTime: String?,
    val locationId: String?,
    val externalUrl: String?,
    val isOutdoor: Boolean,
    val tags: List<String>? = emptyList(),
    val createdAt: Long
)

data class Favourite(
    val id: String,
    val userId: String,
    val itemType: String,
    val itemId: String,
    val savedAt: Long
)

data class CreateFavouriteRequest(
    val itemType: String,
    val itemId: String
)

data class SyncAction(
    val action: String,
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
    val condition: String,
    val temperatureCelsius: Double?,
    val message: String,
    val recommendedLocationIds: List<String>? = emptyList(),
    val recommendedLocations: List<CampusLocation>? = emptyList(),
    val allLocationsWithStatus: List<LocationWeatherInfo>? = emptyList()
)

data class LocationWeatherInfo(
    val location: CampusLocation,
    val status: String // RECOMMENDED | INDOOR | OUTDOOR | LESS_SUITABLE
)

data class EventWeatherWarning(
    val eventId: String,
    val warning: String?
)

data class ApiError(
    val error: String,
    val message: String,
    val status: Int
)
