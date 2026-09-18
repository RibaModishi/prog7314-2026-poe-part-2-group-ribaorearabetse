package com.campuscompanion.api.util

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

/**
 * Starter example for Riba's "Unit testing" deliverable (recommendation, filtering,
 * sorting, duplicate prevention, event-warning logic - see Planning & Design doc,
 * Riba's Part table).
 *
 * This file tests the validation rules that already exist in Validation.kt as a
 * template. Add sibling test files for the business rules you own, e.g.:
 *   - RecommendationRulesTest.kt   -> weather-to-location suitability scoring
 *   - EventSuitabilityTest.kt      -> isOutdoor + weather -> event warning text
 *   - FavouriteRulesTest.kt        -> duplicate-save prevention, item-type checks
 *   - EventOrderingTest.kt         -> "future events first" sorting/formatting
 *
 * Run all tests with: ./gradlew test
 * (uses kotlin.test + ktor-server-test-host, already wired up in build.gradle.kts)
 */
class ValidationTest {

    @Test
    fun `requireValidEmail accepts a normal email`() {
        Validation.requireValidEmail("student@example.ac.za") // should not throw
    }

    @Test
    fun `requireValidEmail rejects a malformed email`() {
        assertFailsWith<BadRequestException> {
            Validation.requireValidEmail("not-an-email")
        }
    }

    @Test
    fun `requireValidLanguage accepts en zu af`() {
        listOf("en", "zu", "af").forEach { Validation.requireValidLanguage(it) }
    }

    @Test
    fun `requireValidLanguage rejects unsupported codes`() {
        assertFailsWith<BadRequestException> {
            Validation.requireValidLanguage("fr")
        }
    }

    @Test
    fun `requireValidLocationCategory rejects unknown categories`() {
        assertFailsWith<BadRequestException> {
            Validation.requireValidLocationCategory("SWIMMING_POOL")
        }
    }

    @Test
    fun `requireValidCoordinates rejects out-of-range latitude`() {
        assertFailsWith<BadRequestException> {
            Validation.requireValidCoordinates(999.0, 31.0)
        }
    }

    @Test
    fun `requireQuietLevel rejects values outside 1 to 5`() {
        assertFailsWith<BadRequestException> {
            Validation.requireQuietLevel(0)
        }
        assertFailsWith<BadRequestException> {
            Validation.requireQuietLevel(6)
        }
    }
}
