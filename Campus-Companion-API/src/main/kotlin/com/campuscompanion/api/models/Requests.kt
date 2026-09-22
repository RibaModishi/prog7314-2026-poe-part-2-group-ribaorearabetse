package com.campuscompanion.api.models

import kotlinx.serialization.Serializable

@Serializable
data class UpsertProfileRequest(
    val name: String,
    val email: String,
    val preferredLanguage: String = SupportedLanguage.DEFAULT
)

@Serializable
data class CreateFavouriteRequest(
    val itemType: String,
    val itemId: String
)

@Serializable
data class CreateLocationRequest(
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
    val quietLevel: Int = 3
)

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String = "",
    val type: String,
    val organiser: String = "",
    val startDateTime: String,
    val endDateTime: String? = null,
    val locationId: String? = null,
    val externalUrl: String? = null,
    val isOutdoor: Boolean = false,
    val tags: List<String> = emptyList()
)
