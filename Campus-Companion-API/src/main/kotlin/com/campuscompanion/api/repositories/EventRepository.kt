package com.campuscompanion.api.repositories

import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.models.EventOrOpportunity
import com.google.cloud.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads/writes the `events` Firestore collection: campus events + external opportunities. */
class EventRepository {

    private fun collection() = FirebaseConfig.firestore().collection(FirestoreCollections.EVENTS)

    /**
     * Filters by [type] and/or a minimum [fromDateTime] (ISO-8601 string, inclusive),
     * matching the brief's "filterable by category and date" requirement.
     * Sorting/"future events first" business rules live with Riba - this simply
     * returns startDateTime-ascending so the client (or Riba's logic) can post-process.
     */
    suspend fun findAll(type: String? = null, fromDateTime: String? = null): List<EventOrOpportunity> =
        withContext(Dispatchers.IO) {
            var query: com.google.cloud.firestore.Query = collection()
            if (type != null) query = query.whereEqualTo("type", type)
            if (fromDateTime != null) query = query.whereGreaterThanOrEqualTo("startDateTime", fromDateTime)
            query = query.orderBy("startDateTime")
            query.get().get().documents.mapNotNull { it.toEvent() }
        }

    suspend fun findById(id: String): EventOrOpportunity? = withContext(Dispatchers.IO) {
        collection().document(id).get().get().toEvent()
    }

    suspend fun findByIds(ids: List<String>): List<EventOrOpportunity> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext emptyList()
        ids.chunked(30).flatMap { chunk ->
            collection().whereIn("__name__", chunk).get().get().documents.mapNotNull { it.toEvent() }
        }
    }

    suspend fun findOutdoorEvents(): List<EventOrOpportunity> = withContext(Dispatchers.IO) {
        collection().whereEqualTo("isOutdoor", true).get().get().documents.mapNotNull { it.toEvent() }
    }

    suspend fun create(event: EventOrOpportunity): EventOrOpportunity = withContext(Dispatchers.IO) {
        val docRef = collection().document()
        val createdAt = System.currentTimeMillis()
        docRef.set(event.toMap(createdAt)).get()
        event.copy(id = docRef.id, createdAt = createdAt)
    }

    private fun EventOrOpportunity.toMap(createdAt: Long): Map<String, Any?> = mapOf(
        "title" to title,
        "description" to description,
        "type" to type,
        "organiser" to organiser,
        "startDateTime" to startDateTime,
        "endDateTime" to endDateTime,
        "locationId" to locationId,
        "externalUrl" to externalUrl,
        "isOutdoor" to isOutdoor,
        "tags" to tags,
        "createdAt" to createdAt
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toEvent(): EventOrOpportunity? {
        if (!exists()) return null
        return EventOrOpportunity(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            type = getString("type") ?: "",
            organiser = getString("organiser") ?: "",
            startDateTime = getString("startDateTime") ?: "",
            endDateTime = getString("endDateTime"),
            locationId = getString("locationId"),
            externalUrl = getString("externalUrl"),
            isOutdoor = getBoolean("isOutdoor") ?: false,
            tags = (get("tags") as? List<String>) ?: emptyList(),
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}
