package com.campuscompanion.api.routes

import com.campuscompanion.api.models.CreateFavouriteRequest
import com.campuscompanion.api.plugins.requireUid
import com.campuscompanion.api.repositories.EventRepository
import com.campuscompanion.api.repositories.FavouriteRepository
import com.campuscompanion.api.repositories.LocationRepository
import com.campuscompanion.api.util.BadRequestException
import com.campuscompanion.api.util.ForbiddenException
import com.campuscompanion.api.util.NotFoundException
import com.campuscompanion.api.util.Validation
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class SyncAction(
    val action: String, // "SAVE" or "REMOVE"
    val itemType: String,
    val itemId: String,
    val clientTimestamp: Long
)

@Serializable
data class SyncRequest(val actions: List<SyncAction>)

@Serializable
data class SyncResult(
    val itemType: String,
    val itemId: String,
    val action: String,
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class SyncResponse(val syncResults: List<SyncResult>)

/**
 * GET    /api/favourites          - list the signed-in user's saved locations/events
 * POST   /api/favourites          - save a location or event
 * DELETE /api/favourites/{id}     - remove a saved item
 * POST   /api/favourites/sync     - replay a batch of offline-queued save/remove actions
 *
 * Every route here requires a valid Firebase ID token: favourites are always
 * scoped to the authenticated caller (never trust an id from the client alone).
 */
fun Route.favouriteRoutes(
    favouriteRepository: FavouriteRepository,
    locationRepository: LocationRepository,
    eventRepository: EventRepository
) {

    get("/api/favourites") {
        val uid = call.requireUid()
        val favourites = favouriteRepository.findByUser(uid)
        call.respond(HttpStatusCode.OK, favourites)
    }

    post("/api/favourites") {
        val uid = call.requireUid()
        val body = call.receive<CreateFavouriteRequest>()

        Validation.requireValidFavouriteItemType(body.itemType)
        Validation.requireNonBlank(body.itemId, "itemId")
        assertReferencedItemExists(body.itemType, body.itemId, locationRepository, eventRepository)

        // Duplicate prevention business rule (favourites are unique per user+item)
        val existing = favouriteRepository.existsForUser(uid, body.itemType, body.itemId)
        val favourite = existing ?: favouriteRepository.create(uid, body.itemType, body.itemId)

        call.respond(HttpStatusCode.Created, favourite)
    }

    delete("/api/favourites/{id}") {
        val uid = call.requireUid()
        val id = call.parameters["id"] ?: throw BadRequestException("Missing favourite id")

        val favourite = favouriteRepository.findById(id) ?: throw NotFoundException("No saved item found with id '$id'")
        if (favourite.userId != uid) {
            throw ForbiddenException("You can only remove your own saved items")
        }

        favouriteRepository.delete(id)
        call.respond(HttpStatusCode.NoContent)
    }

    post("/api/favourites/sync") {
        val uid = call.requireUid()
        val body = call.receive<SyncRequest>()

        val results = body.actions
            .sortedBy { it.clientTimestamp } // apply in the order they happened on-device
            .map { action ->
                try {
                    Validation.requireValidFavouriteItemType(action.itemType)
                    when (action.action.uppercase()) {
                        "SAVE" -> {
                            assertReferencedItemExists(action.itemType, action.itemId, locationRepository, eventRepository)
                            val existing = favouriteRepository.existsForUser(uid, action.itemType, action.itemId)
                            existing ?: favouriteRepository.create(uid, action.itemType, action.itemId)
                            SyncResult(action.itemType, action.itemId, "SAVE", success = true)
                        }
                        "REMOVE" -> {
                            val existing = favouriteRepository.existsForUser(uid, action.itemType, action.itemId)
                            if (existing != null) favouriteRepository.delete(existing.id)
                            SyncResult(action.itemType, action.itemId, "REMOVE", success = true)
                        }
                        else -> SyncResult(action.itemType, action.itemId, action.action, success = false, message = "Unknown action")
                    }
                } catch (ex: Exception) {
                    SyncResult(action.itemType, action.itemId, action.action, success = false, message = ex.message)
                }
            }

        call.respond(HttpStatusCode.OK, SyncResponse(results))
    }
}

private suspend fun assertReferencedItemExists(
    itemType: String,
    itemId: String,
    locationRepository: LocationRepository,
    eventRepository: EventRepository
) {
    val exists = when (itemType) {
        com.campuscompanion.api.models.FavouriteItemType.LOCATION -> locationRepository.findById(itemId) != null
        com.campuscompanion.api.models.FavouriteItemType.EVENT -> eventRepository.findById(itemId) != null
        else -> false
    }
    if (!exists) throw NotFoundException("No $itemType found with id '$itemId'")
}
