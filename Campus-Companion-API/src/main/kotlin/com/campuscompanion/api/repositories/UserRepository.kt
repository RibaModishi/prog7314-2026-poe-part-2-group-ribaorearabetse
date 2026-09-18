package com.campuscompanion.api.repositories

import com.campuscompanion.api.config.FirebaseConfig
import com.campuscompanion.api.models.SupportedLanguage
import com.campuscompanion.api.models.UserProfile
import com.google.cloud.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads/writes the `users` Firestore collection.
 * Document ID == Firebase Auth UID, which keeps a 1:1 lookup trivial and
 * matches the brief's "create/read/update basic user profiles" deliverable.
 */
class UserRepository {

    private fun collection() = FirebaseConfig.firestore().collection(FirestoreCollections.USERS)

    /** Fetches a profile, or null if the user has never been created. */
    suspend fun findByUid(uid: String): UserProfile? = withContext(Dispatchers.IO) {
        val snapshot = collection().document(uid).get().get()
        snapshot.toUserProfile()
    }

    /**
     * Creates the profile on first login, or updates name/email/preferredLanguage
     * on subsequent calls. Mirrors "Fetch (or create on first login) the signed-in
     * user's profile" from the endpoint spec.
     */
    suspend fun upsert(
        uid: String,
        name: String,
        email: String,
        preferredLanguage: String = SupportedLanguage.DEFAULT
    ): UserProfile = withContext(Dispatchers.IO) {
        val docRef = collection().document(uid)
        val existing = docRef.get().get()
        val now = System.currentTimeMillis()
        val createdAt = if (existing.exists()) existing.getLong("createdAt") ?: now else now

        val data = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "preferredLanguage" to preferredLanguage,
            "createdAt" to createdAt,
            "updatedAt" to now
        )
        docRef.set(data).get()

        UserProfile(uid, name, email, preferredLanguage, createdAt, now)
    }

    /** Partial update used by the settings screen (only touches provided fields). */
    suspend fun updateSettings(uid: String, preferredLanguage: String?): UserProfile = withContext(Dispatchers.IO) {
        val docRef = collection().document(uid)
        val updates = mutableMapOf<String, Any>("updatedAt" to System.currentTimeMillis())
        preferredLanguage?.let { updates["preferredLanguage"] = it }
        docRef.set(updates, com.google.cloud.firestore.SetOptions.merge()).get()
        docRef.get().get().toUserProfile()
            ?: throw IllegalStateException("Failed to read back updated profile for uid=$uid")
    }

    private fun DocumentSnapshot.toUserProfile(): UserProfile? {
        if (!exists()) return null
        return UserProfile(
            uid = id,
            name = getString("name") ?: "",
            email = getString("email") ?: "",
            preferredLanguage = getString("preferredLanguage") ?: SupportedLanguage.DEFAULT,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }
}
