package com.campuscompanion.api.routes

import com.campuscompanion.api.models.CampusLocation
import kotlin.test.Test
import kotlin.test.assertEquals

class RecommendationRoutesTest {

    @Test
    fun `weatherStatus returns RECOMMENDED for indoor covered location in RAIN`() {
        val location = CampusLocation(
            name = "Main Library",
            category = "LIBRARY",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = true,
            isCovered = true
        )
        assertEquals(LocationWeatherStatus.RECOMMENDED, location.weatherStatus("RAIN"))
    }

    @Test
    fun `weatherStatus returns INDOOR for indoor location not suitable for CLEAR`() {
        val location = CampusLocation(
            name = "Study Hall",
            category = "STUDY",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = true,
            isCovered = true
        )
        assertEquals(LocationWeatherStatus.INDOOR, location.weatherStatus("CLEAR"))
    }

    @Test
    fun `weatherStatus returns OUTDOOR for covered outdoor location not suitable for COLD`() {
        val location = CampusLocation(
            name = "Covered Courtyard",
            category = "OUTDOOR",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = false,
            isCovered = true
        )
        assertEquals(LocationWeatherStatus.OUTDOOR, location.weatherStatus("COLD"))
    }

    @Test
    fun `weatherStatus returns LESS_SUITABLE for uncovered outdoor location not suitable for RAIN`() {
        val location = CampusLocation(
            name = "Open Lawn",
            category = "OUTDOOR",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = false,
            isCovered = false
        )
        assertEquals(LocationWeatherStatus.LESS_SUITABLE, location.weatherStatus("RAIN"))
    }

    @Test
    fun `weatherStatus returns RECOMMENDED for covered location in RAIN`() {
        val location = CampusLocation(
            name = "Covered Walkway",
            category = "WALKWAY",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = false,
            isCovered = true
        )
        assertEquals(LocationWeatherStatus.RECOMMENDED, location.weatherStatus("RAIN"))
    }

    @Test
    fun `weatherStatus returns RECOMMENDED for air-conditioned location in HOT`() {
        val location = CampusLocation(
            name = "Cool Lab",
            category = "LAB",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = true,
            isCovered = false,
            hasAirConditioning = true
        )
        assertEquals(LocationWeatherStatus.RECOMMENDED, location.weatherStatus("HOT"))
    }

    @Test
    fun `weatherStatus returns RECOMMENDED for indoor location in COLD`() {
        val location = CampusLocation(
            name = "Warm Lounge",
            category = "LOUNGE",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = true,
            isCovered = false
        )
        assertEquals(LocationWeatherStatus.RECOMMENDED, location.weatherStatus("COLD"))
    }

    @Test
    fun `weatherStatus returns RECOMMENDED for covered location in WIND`() {
        val location = CampusLocation(
            name = "Sheltered Plaza",
            category = "PLAZA",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = false,
            isCovered = true
        )
        assertEquals(LocationWeatherStatus.RECOMMENDED, location.weatherStatus("WIND"))
    }

    @Test
    fun `weatherStatus returns RECOMMENDED for outdoor location in CLEAR`() {
        val location = CampusLocation(
            name = "Campus Green",
            category = "OUTDOOR",
            latitude = -29.85,
            longitude = 31.02,
            isIndoor = false,
            isCovered = false
        )
        assertEquals(LocationWeatherStatus.RECOMMENDED, location.weatherStatus("CLEAR"))
    }

    @Test
    fun `eventWeatherWarning returns null for indoor events regardless of condition`() {
        listOf("RAIN", "WIND", "HOT", "CLEAR", "COLD", "UNKNOWN").forEach { condition ->
            assertEquals(null, eventWeatherWarning(isOutdoor = false, condition = condition))
        }
    }

    @Test
    fun `eventWeatherWarning returns rain message for outdoor event in RAIN`() {
        assertEquals(
            "Rain expected \u2014 bring an umbrella.",
            eventWeatherWarning(isOutdoor = true, condition = "RAIN")
        )
    }

    @Test
    fun `eventWeatherWarning returns wind message for outdoor event in WIND`() {
        assertEquals(
            "Strong wind expected \u2014 this outdoor event may be affected.",
            eventWeatherWarning(isOutdoor = true, condition = "WIND")
        )
    }

    @Test
    fun `eventWeatherWarning returns hot message for outdoor event in HOT`() {
        assertEquals(
            "Hot weather expected \u2014 stay hydrated at this outdoor event.",
            eventWeatherWarning(isOutdoor = true, condition = "HOT")
        )
    }

    @Test
    fun `eventWeatherWarning returns clear message for outdoor event in CLEAR`() {
        assertEquals(
            "Suitable weather expected.",
            eventWeatherWarning(isOutdoor = true, condition = "CLEAR")
        )
    }

    @Test
    fun `eventWeatherWarning returns generic message for outdoor event in unrecognised condition`() {
        assertEquals(
            "Weather may affect this event.",
            eventWeatherWarning(isOutdoor = true, condition = "FOG")
        )
    }
}
