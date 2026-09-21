package com.example.campus_companion_prog.data

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.campus_companion_prog.api.CreateFavouriteRequest
import com.example.campus_companion_prog.api.Favourite
import com.example.campus_companion_prog.api.RetrofitClient
import com.example.campus_companion_prog.api.SyncAction
import com.example.campus_companion_prog.api.SyncRequest
import com.example.campus_companion_prog.api.toApiError
import com.example.campus_companion_prog.data.local.AppDatabase
import com.example.campus_companion_prog.data.local.PendingSyncActionEntity
import com.example.campus_companion_prog.sync.FavouriteSyncWorker
import retrofit2.HttpException
import java.io.IOException

/**
 * Favourites with offline-first behaviour: network failures queue Room actions
 * and schedule WorkManager sync when connectivity returns.
 */
class FavouriteOfflineRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).pendingSyncDao()

    companion object {
        private const val TAG = "FavouriteOfflineRepo"
    }

    suspend fun saveFavourite(itemType: String, itemId: String): Result<Favourite> {
        return try {
            val fav = RetrofitClient.api.addFavourite(CreateFavouriteRequest(itemType, itemId))
            Result.success(fav)
        } catch (ex: IOException) {
            Log.w(TAG, "Offline — queueing SAVE $itemType/$itemId")
            queue("SAVE", itemType, itemId)
            scheduleSync()
            Result.failure(IllegalStateException("Saved offline — will sync when you reconnect."))
        } catch (ex: HttpException) {
            Result.failure(IllegalStateException(ex.toApiError()?.message ?: "Could not save (${ex.code()})"))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException(ex.message ?: "Could not save item."))
        }
    }

    suspend fun queueRemove(itemType: String, itemId: String) {
        queue("REMOVE", itemType, itemId)
        scheduleSync()
    }

    suspend fun pendingCount(): Int = dao.count()

    suspend fun flushPending(): Result<Int> {
        val pending = dao.getAll()
        if (pending.isEmpty()) return Result.success(0)
        return try {
            val response = RetrofitClient.api.syncFavourites(
                SyncRequest(
                    pending.map {
                        SyncAction(it.action, it.itemType, it.itemId, it.clientTimestamp)
                    }
                )
            )
            dao.deleteByIds(pending.map { it.id })
            Log.i(TAG, "Synced ${response.syncResults.size} actions")
            Result.success(response.syncResults.size)
        } catch (ex: Exception) {
            Log.e(TAG, "Sync failed", ex)
            Result.failure(ex)
        }
    }

    private suspend fun queue(action: String, itemType: String, itemId: String) {
        dao.insert(
            PendingSyncActionEntity(
                action = action,
                itemType = itemType,
                itemId = itemId,
                clientTimestamp = System.currentTimeMillis()
            )
        )
    }

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<FavouriteSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            FavouriteSyncWorker.UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        Log.i(TAG, "Scheduled FavouriteSyncWorker")
    }
}
