package com.campuscompanion.api.routes

import com.campuscompanion.api.models.CampusLocation
import com.campuscompanion.api.models.EventWeatherWarning
import com.campuscompanion.api.models.LocationWeatherInfo
import com.campuscompanion.api.models.WeatherRecommendation
import com.campuscompanion.api.repositories.EventRepository
import com.campuscompanion.api.repositories.LocationRepository
import com.campuscompanion.api.services.WeatherService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

/**
 * GET /api/recommendations/weather[?mockCondition=RAIN]
 *
 * Fetches current weather for the campus and returns campus locations best
 * suited to it, e.g. indoor/covered spaces when it's raining, outdoor chill
 * spots when it's clear and mild. This is a first working implementation of
 * the rule described in the plan ("Riba's weather rules"); Riba owns refining
 * the scoring logic itself (see services/WeatherService.kt + this file), while
 * this endpoint (Khumo's ownership) stays stable for the Android client.
 *
 * Left public/unauthenticated so it can power the dashboard before sign-in.
 */
fun Route.recommendationRoutes(
    locationRepository: LocationRepository,
    weatherService: WeatherService,
    eventRepository: EventRepository
) {

    get("/api/recommendations/weather") {
        val mockCondition = call.request.queryParameters["mockCondition"]
        val weather = weatherService.currentWeather(mockCondition)
        val allLocations = locationRepository.findAll()
        val allLocationsWithStatus = allLocations.map {
            LocationWeatherInfo(it, it.weatherStatus(weather.condition))
        }

        val recommended = allLocations.filter { it.suitableFor(weather.condition) }
            .ifEmpty { allLocations } // never return an empty list if we have no perfect match

        val message = buildMessage(weather.condition, weather.rawDescription)

        call.respond(
            HttpStatusCode.OK,
            WeatherRecommendation(
                condition = weather.condition,
                temperatureCelsius = weather.temperatureCelsius,
                message = message,
                recommendedLocationIds = recommended.map { it.id },
                recommendedLocations = recommended,
                allLocationsWithStatus = allLocationsWithStatus
            )
        )
    }

    get("/api/recommendations/events-weather") {
        val mockCondition = call.request.queryParameters["mockCondition"]
        val weather = weatherService.currentWeather(mockCondition)
        val events = eventRepository.findAll()
        val warnings = events.map {
            EventWeatherWarning(it.id, eventWeatherWarning(it.isOutdoor, weather.condition))
        }
        call.respond(HttpStatusCode.OK, warnings)
    }
}

@Serializable
enum class LocationWeatherStatus {
    RECOMMENDED,
    INDOOR,
    OUTDOOR,
    LESS_SUITABLE
}

internal fun CampusLocation.suitableFor(condition: String): Boolean = when (condition) {
    "RAIN" -> isIndoor || isCovered
    "HOT" -> hasAirConditioning || isCovered
    "COLD" -> isIndoor
    "WIND" -> isIndoor || isCovered
    "CLEAR" -> !isIndoor // highlight outdoor chill spots when it's clear and mild
    else -> true
}

internal fun CampusLocation.weatherStatus(condition: String): LocationWeatherStatus = when {
    suitableFor(condition) -> LocationWeatherStatus.RECOMMENDED
    isIndoor -> LocationWeatherStatus.INDOOR
    isCovered -> LocationWeatherStatus.OUTDOOR
    else -> LocationWeatherStatus.LESS_SUITABLE
}

internal fun eventWeatherWarning(isOutdoor: Boolean, condition: String): String? {
    if (!isOutdoor) return null
    return when (condition) {
        "RAIN" -> "Rain expected \u2014 bring an umbrella."
        "WIND" -> "Strong wind expected \u2014 this outdoor event may be affected."
        "HOT" -> "Hot weather expected \u2014 stay hydrated at this outdoor event."
        "CLEAR" -> "Suitable weather expected."
        else -> "Weather may affect this event."
    }
}

private fun buildMessage(condition: String, rawDescription: String): String = when (condition) {
    "RAIN" -> "Rain is expected ($rawDescription). Indoor study spaces and libraries are recommended."
    "HOT" -> "It's hot out there ($rawDescription). Air-conditioned or shaded places are recommended."
    "COLD" -> "It's cold ($rawDescription). Indoor study rooms, libraries and cafeterias are recommended."
    "WIND" -> "Strong wind expected ($rawDescription). Indoor and sheltered facilities are recommended."
    "CLEAR" -> "Clear, mild weather ($rawDescription). Great time for outdoor chill spots and events."
    else -> "Current weather: $rawDescription."
}
