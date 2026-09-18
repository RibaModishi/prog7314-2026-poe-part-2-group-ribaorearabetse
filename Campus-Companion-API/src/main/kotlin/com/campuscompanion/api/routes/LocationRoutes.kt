package com.campuscompanion.api.routes

import com.campuscompanion.api.models.CampusLocation
import com.campuscompanion.api.models.CreateLocationRequest
import com.campuscompanion.api.repositories.LocationRepository
import com.campuscompanion.api.util.NotFoundException
import com.campuscompanion.api.util.Validation
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * GET  /api/locations                       - all campus locations (for map markers)
 * GET  /api/locations?category=STUDY_SPACE  - filtered by category
 * GET  /api/locations/{id}                  - one location's full details
 * POST /api/locations                       - create a location (used by seeding/admin tooling)
 *
 * Left public/unauthenticated by design (per the "Optional/public - team decision" note
 * in the plan) so the Campus Navigator can be browsed before sign-in, mirroring the
 * MyUoY behaviour highlighted in our research.
 */
fun Route.locationRoutes(locationRepository: LocationRepository) {

    get("/api/locations") {
        val category = call.request.queryParameters["category"]
        category?.let { Validation.requireValidLocationCategory(it) }

        val locations = locationRepository.findAll(category)
        call.respond(HttpStatusCode.OK, locations)
    }

    get("/api/locations/{id}") {
        val id = call.parameters["id"] ?: throw com.campuscompanion.api.util.BadRequestException("Missing location id")
        val location = locationRepository.findById(id)
            ?: throw NotFoundException("No location found with id '$id'")
        call.respond(HttpStatusCode.OK, location)
    }

    post("/api/locations") {
        val body = call.receive<CreateLocationRequest>()

        Validation.requireNonBlank(body.name, "name")
        Validation.requireValidLocationCategory(body.category)
        Validation.requireValidCoordinates(body.latitude, body.longitude)
        Validation.requireQuietLevel(body.quietLevel)

        val created = locationRepository.create(
            CampusLocation(
                name = body.name,
                category = body.category,
                description = body.description,
                latitude = body.latitude,
                longitude = body.longitude,
                openingHours = body.openingHours,
                isIndoor = body.isIndoor,
                isCovered = body.isCovered,
                hasAirConditioning = body.hasAirConditioning,
                hasWifi = body.hasWifi,
                quietLevel = body.quietLevel
            )
        )
        call.respond(HttpStatusCode.Created, created)
    }
}
