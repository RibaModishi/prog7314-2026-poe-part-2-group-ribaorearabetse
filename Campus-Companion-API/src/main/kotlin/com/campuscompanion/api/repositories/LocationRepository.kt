package com.campuscompanion.api.repositories

import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.models.CampusLocation
import com.google.cloud.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads/writes the `locations` Firestore collection backing the Campus Navigator. */
class LocationRepository {

    private fun collection() = FirebaseConfig.firestore().collection(FirestoreCollections.LOCATIONS)

    suspend fun findAll(category: String? = null): List<CampusLocation> = withContext(Dispatchers.IO) {
        val query = if (category != null) {
            collection().whereEqualTo("category", category)
        } else {
            collection()
        }
        query.get().get().documents.mapNotNull { it.toLocation() }
    }

    suspend fun findById(id: String): CampusLocation? = withContext(Dispatchers.IO) {
        collection().document(id).get().get().toLocation()
    }

    /** Firestore's whereIn supports at most 30 values, which comfortably covers a saved-items list. */
    suspend fun findByIds(ids: List<String>): List<CampusLocation> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext emptyList()
        ids.chunked(30).flatMap { chunk ->
            collection().whereIn("__name__", chunk).get().get().documents.mapNotNull { it.toLocation() }
        }
    }

    suspend fun create(location: CampusLocation): CampusLocation = withContext(Dispatchers.IO) {
        val docRef = collection().document()
        val data = location.toMap(createdAt = System.currentTimeMillis())
        docRef.set(data).get()
        location.copy(id = docRef.id, createdAt = data["createdAt"] as Long)
    }

    private fun CampusLocation.toMap(createdAt: Long): Map<String, Any> = mapOf(
        "name" to name,
        "category" to category,
        "description" to description,
        "latitude" to latitude,
        "longitude" to longitude,
        "openingHours" to openingHours,
        "isIndoor" to isIndoor,
        "isCovered" to isCovered,
        "hasAirConditioning" to hasAirConditioning,
        "hasWifi" to hasWifi,
        "quietLevel" to quietLevel.toLong(),
        "createdAt" to createdAt
    )

    private fun DocumentSnapshot.toLocation(): CampusLocation? {
        if (!exists()) return null
        return CampusLocation(
            id = id,
            name = getString("name") ?: "",
            category = getString("category") ?: "",
            description = getString("description") ?: "",
            latitude = getDouble("latitude") ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            openingHours = getString("openingHours") ?: "",
            isIndoor = getBoolean("isIndoor") ?: true,
            isCovered = getBoolean("isCovered") ?: true,
            hasAirConditioning = getBoolean("hasAirConditioning") ?: false,
            hasWifi = getBoolean("hasWifi") ?: true,
            quietLevel = (getLong("quietLevel") ?: 3L).toInt(),
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}
