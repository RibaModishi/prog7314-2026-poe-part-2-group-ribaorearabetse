package com.example.campus_companion_prog.domain

import com.example.campus_companion_prog.api.CampusLocation
import com.example.campus_companion_prog.api.EventOrOpportunity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRulesTest {

    private fun location(
        indoor: Boolean = false,
        covered: Boolean = false,
        ac: Boolean = false
    ) = CampusLocation(
        id = "1",
        name = "Test Spot",
        category = "CHILL_SPOT",
        description = "desc",
        latitude = -26.0,
        longitude = 28.0,
        openingHours = "08:00-17:00",
        isIndoor = indoor,
        isCovered = covered,
        hasAirConditioning = ac,
        hasWifi = true,
        quietLevel = 3,
        createdAt = 0L
    )

    private fun event(outdoor: Boolean) = EventOrOpportunity(
        id = "e1",
        title = "Test Event",
        description = "desc",
        type = "CAMPUS_EVENT",
        organiser = "SRC",
        startDateTime = "2026-10-01T10:00:00Z",
        endDateTime = null,
        locationId = null,
        externalUrl = null,
        isOutdoor = outdoor,
        tags = emptyList(),
        createdAt = 0L
    )

    @Test
    fun rainPrefersIndoorOrCovered() {
        assertTrue(WeatherRules.isSuitable(location(indoor = true), "RAIN"))
        assertTrue(WeatherRules.isSuitable(location(covered = true), "RAIN"))
        assertFalse(WeatherRules.isSuitable(location(), "RAIN"))
    }

    @Test
    fun hotPrefersAirconOrCovered() {
        assertTrue(WeatherRules.isSuitable(location(ac = true), "HOT"))
        assertTrue(WeatherRules.isSuitable(location(covered = true), "HOT"))
        assertFalse(WeatherRules.isSuitable(location(indoor = true), "HOT"))
    }

    @Test
    fun clearHighlightsOutdoor() {
        assertTrue(WeatherRules.isSuitable(location(indoor = false), "CLEAR"))
        assertFalse(WeatherRules.isSuitable(location(indoor = true), "CLEAR"))
    }

    @Test
    fun locationStatusRecommendedWhenSuitable() {
        assertEquals(
            WeatherRules.STATUS_RECOMMENDED,
            WeatherRules.locationStatus(location(indoor = true), "RAIN")
        )
    }

    @Test
    fun locationStatusIndoorWhenNotSuitableButIndoor() {
        assertEquals(
            WeatherRules.STATUS_INDOOR,
            WeatherRules.locationStatus(location(indoor = true), "CLEAR")
        )
    }

    @Test
    fun outdoorEventGetsRainWarning() {
        val warning = WeatherRules.eventWeatherIndicator(event(outdoor = true), "RAIN")
        assertNotNull(warning)
        assertTrue(warning!!.contains("umbrella", ignoreCase = true))
    }

    @Test
    fun indoorEventHasNoWarning() {
        assertNull(WeatherRules.eventWeatherIndicator(event(outdoor = false), "RAIN"))
    }
}
