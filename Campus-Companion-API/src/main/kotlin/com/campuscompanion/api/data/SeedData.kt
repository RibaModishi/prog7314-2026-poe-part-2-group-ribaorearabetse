package com.campuscompanion.api.data

import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.repositories.EventRepository
import com.campuscompanion.api.repositories.LocationRepository
import com.campuscompanion.api.models.CampusLocation
import com.campuscompanion.api.models.EventOrOpportunity
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Seeds Firestore with demo-ready sample data: 10 campus locations covering
 * every required category, and 8 events/opportunities covering every required
 * type. Run with:
 *
 *   ./gradlew seed
 *
 * Safe to re-run: each collection is only seeded if it is currently empty, so
 * this will not create duplicate demo records on a second run.
 */
fun main() = runBlocking {
    val logger = LoggerFactory.getLogger("SeedData")

    if (!FirebaseConfig.isReady) {
        logger.error("Cannot seed: {}", FirebaseConfig.statusMessage)
        logger.error("Add a real firebase-service-account.json to the project root and try again.")
        return@runBlocking
    }

    val locationRepository = LocationRepository()
    val eventRepository = EventRepository()

    val existingLocations = locationRepository.findAll()
    if (existingLocations.isEmpty()) {
        logger.info("Seeding {} campus locations...", sampleLocations.size)
        sampleLocations.forEach { locationRepository.create(it) }
        logger.info("Locations seeded.")
    } else {
        logger.info("Skipping location seed - {} locations already exist.", existingLocations.size)
    }

    val existingEvents = eventRepository.findAll()
    if (existingEvents.isEmpty()) {
        logger.info("Seeding {} events/opportunities...", sampleEvents.size)
        sampleEvents.forEach { eventRepository.create(it) }
        logger.info("Events/opportunities seeded.")
    } else {
        logger.info("Skipping event seed - {} events already exist.", existingEvents.size)
    }

    logger.info("Done.")
}

private val sampleLocations = listOf(
    CampusLocation(
        name = "Main Library - Quiet Study Floor",
        category = "LIBRARY",
        description = "Silent study floor with individual desks and reference sections.",
        latitude = -29.8587, longitude = 31.0218,
        openingHours = "Mon-Fri 07:00-22:00, Sat 08:00-16:00",
        isIndoor = true, isCovered = true, hasAirConditioning = true, hasWifi = true, quietLevel = 5
    ),
    CampusLocation(
        name = "Library Group Study Pods",
        category = "STUDY_SPACE",
        description = "Bookable glass-walled pods for group work, seats 4-6.",
        latitude = -29.8589, longitude = 31.0221,
        openingHours = "Mon-Fri 07:00-22:00",
        isIndoor = true, isCovered = true, hasAirConditioning = true, hasWifi = true, quietLevel = 3
    ),
    CampusLocation(
        name = "Computer Lab 2 (IT Building)",
        category = "LAB",
        description = "General-access computer lab with 60 workstations and design software.",
        latitude = -29.8601, longitude = 31.0233,
        openingHours = "Mon-Fri 08:00-20:00",
        isIndoor = true, isCovered = true, hasAirConditioning = true, hasWifi = true, quietLevel = 3
    ),
    CampusLocation(
        name = "Student Centre Cafeteria",
        category = "CAFETERIA",
        description = "Main dining hall with hot meals, coffee bar and vending machines.",
        latitude = -29.8595, longitude = 31.0210,
        openingHours = "Mon-Sat 07:00-19:00",
        isIndoor = true, isCovered = true, hasAirConditioning = false, hasWifi = true, quietLevel = 1
    ),
    CampusLocation(
        name = "Visitor & Student Parking Lot B",
        category = "PARKING",
        description = "Open-air parking, permit required after 08:00.",
        latitude = -29.8578, longitude = 31.0195,
        openingHours = "24/7",
        isIndoor = false, isCovered = false, hasAirConditioning = false, hasWifi = false, quietLevel = 1
    ),
    CampusLocation(
        name = "Student Support & Wellness Office",
        category = "STUDENT_SUPPORT",
        description = "Academic advising, counselling and financial aid queries.",
        latitude = -29.8593, longitude = 31.0227,
        openingHours = "Mon-Fri 08:30-16:30",
        isIndoor = true, isCovered = true, hasAirConditioning = true, hasWifi = true, quietLevel = 4
    ),
    CampusLocation(
        name = "Lecture Venue LT1",
        category = "LECTURE_VENUE",
        description = "300-seat tiered lecture theatre, primary venue for first-year modules.",
        latitude = -29.8605, longitude = 31.0240,
        openingHours = "Mon-Fri 07:00-18:00",
        isIndoor = true, isCovered = true, hasAirConditioning = true, hasWifi = true, quietLevel = 4
    ),
    CampusLocation(
        name = "Courtyard Chill Spot",
        category = "CHILL_SPOT",
        description = "Shaded outdoor courtyard with benches and grass area, popular between classes.",
        latitude = -29.8590, longitude = 31.0215,
        openingHours = "24/7",
        isIndoor = false, isCovered = true, hasAirConditioning = false, hasWifi = true, quietLevel = 2
    ),
    CampusLocation(
        name = "Rooftop Garden Chill Spot",
        category = "CHILL_SPOT",
        description = "Open-air rooftop seating area with campus views, best in clear weather.",
        latitude = -29.8583, longitude = 31.0202,
        openingHours = "Mon-Fri 08:00-18:00",
        isIndoor = false, isCovered = false, hasAirConditioning = false, hasWifi = false, quietLevel = 2
    ),
    CampusLocation(
        name = "Engineering Lab 1",
        category = "LAB",
        description = "Specialised lab with workbenches for practicals and project work.",
        latitude = -29.8611, longitude = 31.0245,
        openingHours = "Mon-Fri 08:00-17:00",
        isIndoor = true, isCovered = true, hasAirConditioning = false, hasWifi = true, quietLevel = 3
    )
)

private val sampleEvents = listOf(
    EventOrOpportunity(
        title = "Welcome Week Braai & Social",
        description = "Kick off the semester with music, food and club sign-ups on the main lawn.",
        type = "CAMPUS_EVENT",
        organiser = "Student Representative Council",
        startDateTime = "2026-10-05T16:00:00Z",
        endDateTime = "2026-10-05T20:00:00Z",
        isOutdoor = true,
        tags = listOf("social", "welcome")
    ),
    EventOrOpportunity(
        title = "NSFAS & Private Bursary Info Session",
        description = "Learn how to apply for NSFAS and partner-company bursaries for next year.",
        type = "BURSARY",
        organiser = "Financial Aid Office",
        startDateTime = "2026-10-08T13:00:00Z",
        isOutdoor = false,
        tags = listOf("funding", "bursary")
    ),
    EventOrOpportunity(
        title = "Software Engineering Internship - Vodacom",
        description = "12-month paid internship for final-year IT/Computer Science students.",
        type = "INTERNSHIP",
        organiser = "Vodacom",
        startDateTime = "2026-10-01T00:00:00Z",
        endDateTime = "2026-11-15T00:00:00Z",
        externalUrl = "https://careers.vodacom.co.za",
        isOutdoor = false,
        tags = listOf("engineering", "funded", "IT")
    ),
    EventOrOpportunity(
        title = "CV & LinkedIn Workshop",
        description = "Hands-on workshop covering CV writing and LinkedIn optimisation for graduate applications.",
        type = "WORKSHOP",
        organiser = "Career Development Centre",
        startDateTime = "2026-10-10T10:00:00Z",
        endDateTime = "2026-10-10T12:00:00Z",
        isOutdoor = false,
        tags = listOf("careers", "workshop")
    ),
    EventOrOpportunity(
        title = "Robotics Society Weekly Meetup",
        description = "Weekly build session for the campus robotics club - all skill levels welcome.",
        type = "SOCIETY_ACTIVITY",
        organiser = "Robotics Society",
        startDateTime = "2026-10-06T17:00:00Z",
        endDateTime = "2026-10-06T19:00:00Z",
        isOutdoor = false,
        tags = listOf("society", "robotics")
    ),
    EventOrOpportunity(
        title = "Outdoor Movie Night",
        description = "Screening on the main lawn - bring a blanket, weather permitting.",
        type = "CAMPUS_EVENT",
        organiser = "Student Activities",
        startDateTime = "2026-10-12T19:00:00Z",
        endDateTime = "2026-10-12T21:30:00Z",
        isOutdoor = true,
        tags = listOf("social", "outdoor")
    ),
    EventOrOpportunity(
        title = "Anglo American Engineering Bursary",
        description = "Full-cost bursary covering tuition, accommodation and a living allowance.",
        type = "BURSARY",
        organiser = "Anglo American",
        startDateTime = "2026-09-01T00:00:00Z",
        endDateTime = "2026-10-31T00:00:00Z",
        externalUrl = "https://www.angloamerican.com/careers/bursaries",
        isOutdoor = false,
        tags = listOf("engineering", "funded")
    ),
    EventOrOpportunity(
        title = "Outdoor Fitness Bootcamp",
        description = "Free weekly bootcamp session run by the Sport Club on the sports field.",
        type = "SOCIETY_ACTIVITY",
        organiser = "Sport Club",
        startDateTime = "2026-10-07T06:30:00Z",
        endDateTime = "2026-10-07T07:30:00Z",
        isOutdoor = true,
        tags = listOf("fitness", "society")
    )
)
