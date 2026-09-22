package com.example.campus_companion_prog.domain

import android.content.Context
import android.graphics.Color
import androidx.annotation.StringRes
import com.example.campus_companion_prog.R
import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.api.EventOrOpportunity

/**
 * Weather-aware campus business rules (Riba ownership).
 */
object WeatherRules {

    const val STATUS_RECOMMENDED = "RECOMMENDED"
    const val STATUS_INDOOR = "INDOOR"
    const val STATUS_OUTDOOR = "OUTDOOR"
    const val STATUS_LESS_SUITABLE = "LESS_SUITABLE"

    fun markerColor(status: String): Int = when (status.uppercase()) {
        STATUS_RECOMMENDED -> Color.parseColor("#16A34A")
        STATUS_INDOOR -> Color.parseColor("#2563EB")
        STATUS_OUTDOOR -> Color.parseColor("#EA580C")
        else -> Color.parseColor("#9CA3AF")
    }

    @StringRes
    fun markerLabelRes(status: String): Int = when (status.uppercase()) {
        STATUS_RECOMMENDED -> R.string.marker_recommended
        STATUS_INDOOR -> R.string.marker_indoor
        STATUS_OUTDOOR -> R.string.marker_outdoor
        else -> R.string.marker_less_suitable
    }

    fun markerLabel(status: String): String = when (status.uppercase()) {
        STATUS_RECOMMENDED -> "Recommended"
        STATUS_INDOOR -> "Indoor"
        STATUS_OUTDOOR -> "Outdoor"
        else -> "Less suitable"
    }

    fun isSuitable(location: CampusLocation, condition: String): Boolean = when (condition.uppercase()) {
        "RAIN" -> location.isIndoor || location.isCovered
        "HOT" -> location.hasAirConditioning || location.isCovered
        "COLD" -> location.isIndoor
        "WIND" -> location.isIndoor || location.isCovered
        "CLEAR" -> !location.isIndoor
        else -> true
    }

    fun locationStatus(location: CampusLocation, condition: String): String = when {
        isSuitable(location, condition) -> STATUS_RECOMMENDED
        location.isIndoor -> STATUS_INDOOR
        location.isCovered -> STATUS_OUTDOOR
        else -> STATUS_LESS_SUITABLE
    }

    @StringRes
    fun eventWeatherIndicatorRes(event: EventOrOpportunity, condition: String): Int? {
        if (!event.isOutdoor) return null
        return when (condition.uppercase()) {
            "RAIN" -> R.string.weather_banner_rain
            "WIND" -> R.string.weather_banner_wind
            "HOT" -> R.string.weather_banner_hot
            "CLEAR" -> R.string.weather_banner_clear
            "COLD" -> R.string.weather_banner_cold
            else -> R.string.weather_banner_generic
        }
    }

    /** English text for unit tests / fallback. */
    fun eventWeatherIndicator(event: EventOrOpportunity, condition: String): String? {
        val res = eventWeatherIndicatorRes(event, condition) ?: return null
        return when (res) {
            R.string.weather_banner_rain -> "Rain expected — bring an umbrella."
            R.string.weather_banner_wind -> "Strong wind expected — this outdoor event may be affected."
            R.string.weather_banner_hot -> "Hot weather expected — stay hydrated at this outdoor event."
            R.string.weather_banner_clear -> "Suitable weather expected."
            R.string.weather_banner_cold -> "Cold weather expected — dress warmly for this outdoor event."
            else -> "Weather may affect this event."
        }
    }

    @StringRes
    fun conditionDisplayNameRes(condition: String): Int = when (condition.uppercase()) {
        "RAIN" -> R.string.condition_rain
        "HOT" -> R.string.condition_hot
        "COLD" -> R.string.condition_cold
        "WIND" -> R.string.condition_wind
        "CLEAR" -> R.string.condition_clear
        else -> R.string.condition_clear
    }

    fun conditionDisplayName(condition: String): String = when (condition.uppercase()) {
        "RAIN" -> "Rain"
        "HOT" -> "Hot"
        "COLD" -> "Cold"
        "WIND" -> "Windy"
        "CLEAR" -> "Clear"
        else -> condition
    }

    fun localizedMarkerLabel(context: Context, status: String): String =
        context.getString(markerLabelRes(status))

    fun localizedCondition(context: Context, condition: String): String =
        context.getString(conditionDisplayNameRes(condition))

    fun localizedEventBanner(context: Context, event: EventOrOpportunity, condition: String): String? {
        val res = eventWeatherIndicatorRes(event, condition) ?: return null
        return context.getString(res)
    }
}
