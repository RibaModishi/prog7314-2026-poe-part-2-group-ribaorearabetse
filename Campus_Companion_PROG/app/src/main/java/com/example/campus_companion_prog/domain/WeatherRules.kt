package com.example.campus_companion_prog.domain

import android.graphics.Color
import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.api.EventOrOpportunity

/**
 * Weather-aware campus business rules (Riba ownership).
 *
 * Mirrors the Ktor recommendation rules so Android can:
 *  - colour smart map markers
 *  - show outdoor-event weather indicators
 *  - unit-test scoring without hitting the network
 *
 * Marker colours (assignment innovative features):
 *  - Green  → recommended for current weather
 *  - Blue   → indoor facility
 *  - Orange → outdoor / covered outdoor
 *  - Grey   → less suitable under current weather
 */
object WeatherRules {

    const val STATUS_RECOMMENDED = "RECOMMENDED"
    const val STATUS_INDOOR = "INDOOR"
    const val STATUS_OUTDOOR = "OUTDOOR"
    const val STATUS_LESS_SUITABLE = "LESS_SUITABLE"

    /** Marker / badge colour for a location weather status. */
    fun markerColor(status: String): Int = when (status.uppercase()) {
        STATUS_RECOMMENDED -> Color.parseColor("#16A34A") // green
        STATUS_INDOOR -> Color.parseColor("#2563EB") // blue
        STATUS_OUTDOOR -> Color.parseColor("#EA580C") // orange
        else -> Color.parseColor("#9CA3AF") // grey
    }

    fun markerLabel(status: String): String = when (status.uppercase()) {
        STATUS_RECOMMENDED -> "Recommended"
        STATUS_INDOOR -> "Indoor"
        STATUS_OUTDOOR -> "Outdoor"
        else -> "Less suitable"
    }

    /**
     * Whether a campus location is a good match for the given weather condition.
     * Conditions: RAIN, HOT, COLD, WIND, CLEAR.
     */
    fun isSuitable(location: CampusLocation, condition: String): Boolean = when (condition.uppercase()) {
        "RAIN" -> location.isIndoor || location.isCovered
        "HOT" -> location.hasAirConditioning || location.isCovered
        "COLD" -> location.isIndoor
        "WIND" -> location.isIndoor || location.isCovered
        "CLEAR" -> !location.isIndoor
        else -> true
    }

    /** Compute smart-marker status for a location under a weather condition. */
    fun locationStatus(location: CampusLocation, condition: String): String = when {
        isSuitable(location, condition) -> STATUS_RECOMMENDED
        location.isIndoor -> STATUS_INDOOR
        location.isCovered -> STATUS_OUTDOOR
        else -> STATUS_LESS_SUITABLE
    }

    /**
     * Weather-aware event indicator text for outdoor events.
     * Indoor/online events return null (no banner needed).
     */
    fun eventWeatherIndicator(event: EventOrOpportunity, condition: String): String? {
        if (!event.isOutdoor) return null
        return when (condition.uppercase()) {
            "RAIN" -> "Rain expected — bring an umbrella."
            "WIND" -> "Strong wind expected — this outdoor event may be affected."
            "HOT" -> "Hot weather expected — stay hydrated at this outdoor event."
            "CLEAR" -> "Suitable weather expected."
            "COLD" -> "Cold weather expected — dress warmly for this outdoor event."
            else -> "Weather may affect this event."
        }
    }

    fun conditionDisplayName(condition: String): String = when (condition.uppercase()) {
        "RAIN" -> "Rain"
        "HOT" -> "Hot"
        "COLD" -> "Cold"
        "WIND" -> "Windy"
        "CLEAR" -> "Clear"
        else -> condition
    }
}
