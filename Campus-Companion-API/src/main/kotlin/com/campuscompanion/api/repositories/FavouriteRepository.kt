package com.campuscompanion.api.repositories

import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.models.Favourite
import com.google.cloud.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads/writes the `favourites` Firestore collection: a user's saved locations/events.
 * This is also the server-side target for the Android app's offline-then-sync
 * queue (POST/DELETE calls replay here once connectivity returns).
 */
class FavouriteRepository {

    private fun collection() = FirebaseConfig.firestore().collection(FirestoreCollections.FAVOURITES)

    suspend fun findByUser(userId: String): List<Favourite> = withContext(Dispatchers.IO) {
        collection().whereEqualTo("userId", userId).get().get().documents.mapNotNull { it.toFavourite() }
    }

    suspend fun findById(id: String): Favourite? = withContext(Dispatchers.IO) {
        collection().document(id).get().get().toFavourite()
    }

    /** Prevents duplicate saves of the same item by the same user (business rule owned jointly with Riba). */
    suspend fun existsForUser(userId: String, itemType: String, itemId: String): Favourite? = withContext(Dispatchers.IO) {
        collection()
            .whereEqualTo("userId", userId)
            .whereEqualTo("itemType", itemType)
            .whereEqualTo("itemId", itemId)
            .limit(1)
            .get().get()
            .documents.firstOrNull()?.toFavourite()
    }

    suspend fun create(userId: String, itemType: String, itemId: String): Favourite = withContext(Dispatchers.IO) {
        val docRef = collection().document()
        val savedAt = System.currentTimeMillis()
        docRef.set(
            mapOf(
                "userId" to userId,
                "itemType" to itemType,
                "itemId" to itemId,
                "savedAt" to savedAt
            )
        ).get()
        Favourite(docRef.id, userId, itemType, itemId, savedAt)
    }

    /** Returns true if a document was found and deleted, false if it didn't exist. */
    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val docRef = collection().document(id)
        val existed = docRef.get().get().exists()
        if (existed) docRef.delete().get()
        existed
    }

    private fun DocumentSnapshot.toFavourite(): Favourite? {
        if (!exists()) return null
        return Favourite(
            id = id,
            userId = getString("userId") ?: "",
            itemType = getString("itemType") ?: "",
            itemId = getString("itemId") ?: "",
            savedAt = getLong("savedAt") ?: 0L
        )
    }
}
