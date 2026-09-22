package com.campuscompanion.api.routes

import com.campuscompanion.api.models.CreateEventRequest
import com.campuscompanion.api.models.EventOrOpportunity
import com.campuscompanion.api.repositories.EventRepository
import com.campuscompanion.api.util.BadRequestException
import com.campuscompanion.api.util.NotFoundException
import com.campuscompanion.api.util.Validation
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * GET  /api/events                                   - combined events/opportunities feed
 * GET  /api/events?category=BURSARY                  - filtered by type
 * GET  /api/events?date=2026-10-01T00:00:00Z          - only items on/after this date
 * GET  /api/events/{id}                               - one event/opportunity's full details
 * POST /api/events                                    - create an event/opportunity (seeding/admin)
 *
 * Left public/unauthenticated by design so the feed can be browsed before sign-in.
 * "Suitable for the weather" business logic is layered on top by
 * GET /api/recommendations/weather rather than duplicated here.
 */
fun Route.eventRoutes(eventRepository: EventRepository) {

    get("/api/events") {
        val type = call.request.queryParameters["category"]
        val fromDate = call.request.queryParameters["date"]
        type?.let { Validation.requireValidItemType(it) }

        val events = eventRepository.findAll(type, fromDate)
        call.respond(HttpStatusCode.OK, events)
    }

    get("/api/events/{id}") {
        val id = call.parameters["id"] ?: throw BadRequestException("Missing event id")
        val event = eventRepository.findById(id) ?: throw NotFoundException("No event found with id '$id'")
        call.respond(HttpStatusCode.OK, event)
    }

    post("/api/events") {
        val body = call.receive<CreateEventRequest>()

        Validation.requireNonBlank(body.title, "title")
        Validation.requireValidItemType(body.type)
        Validation.requireNonBlank(body.startDateTime, "startDateTime")

        val created = eventRepository.create(
            EventOrOpportunity(
                title = body.title,
                description = body.description,
                type = body.type,
                organiser = body.organiser,
                startDateTime = body.startDateTime,
                endDateTime = body.endDateTime,
                locationId = body.locationId,
                externalUrl = body.externalUrl,
                isOutdoor = body.isOutdoor,
                tags = body.tags
            )
        )
        call.respond(HttpStatusCode.Created, created)
    }
}
