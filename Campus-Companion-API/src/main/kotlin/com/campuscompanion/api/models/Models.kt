package com.campuscompanion.api.models

import com.campuscompanion.api.routes.LocationWeatherStatus
import kotlinx.serialization.Serializable

/** Allowed campus location categories (kept as plain strings in Firestore for flexibility). */
object LocationCategory {
    val ALL = setOf(
        "LIBRARY", "STUDY_SPACE", "LAB", "CAFETERIA",
        "PARKING", "STUDENT_SUPPORT", "LECTURE_VENUE", "CHILL_SPOT"
    )
}

/** Allowed event/opportunity types. */
object ItemType {
    val ALL = setOf("CAMPUS_EVENT", "BURSARY", "INTERNSHIP", "WORKSHOP", "SOCIETY_ACTIVITY")
}

/** Allowed favourite item types (what a saved item points to). */
object FavouriteItemType {
    const val LOCATION = "LOCATION"
    const val EVENT = "EVENT"
    val ALL = setOf(LOCATION, EVENT)
}

/** Supported UI languages, per the brief's multi-language requirement. */
object SupportedLanguage {
    val ALL = setOf("en", "zu", "af")
    const val DEFAULT = "en"
}

@Serializable
data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val preferredLanguage: String = SupportedLanguage.DEFAULT,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class CampusLocation(
    val id: String = "",
    val name: String,
    val category: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val openingHours: String = "",
    val isIndoor: Boolean = true,
    val isCovered: Boolean = true,
    val hasAirConditioning: Boolean = false,
    val hasWifi: Boolean = true,
    val quietLevel: Int = 3,
    val createdAt: Long = 0L
)

@Serializable
data class EventOrOpportunity(
    val id: String = "",
    val title: String,
    val description: String = "",
    val type: String,
    val organiser: String = "",
    val startDateTime: String,
    val endDateTime: String? = null,
    val locationId: String? = null,
    val externalUrl: String? = null,
    val isOutdoor: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Long = 0L
)

@Serializable
data class Favourite(
    val id: String = "",
    val userId: String,
    val itemType: String,
    val itemId: String,
    val savedAt: Long = 0L
)

@Serializable
data class LocationWeatherInfo(
    val location: CampusLocation,
    val status: LocationWeatherStatus
)

@Serializable
data class WeatherRecommendation(
    val condition: String,
    val temperatureCelsius: Double? = null,
    val message: String,
    val recommendedLocationIds: List<String>,
    val recommendedLocations: List<CampusLocation> = emptyList(),
    val allLocationsWithStatus: List<LocationWeatherInfo> = emptyList()
)

@Serializable
data class EventWeatherWarning(
    val eventId: String,
    val warning: String?
)

@Serializable
data class ApiError(
    val error: String,
    val message: String,
    val status: Int
)
