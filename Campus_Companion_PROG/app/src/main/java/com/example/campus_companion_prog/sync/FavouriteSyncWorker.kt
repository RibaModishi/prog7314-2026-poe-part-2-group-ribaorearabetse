package com.example.campus_companion_prog.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.campus_companion_prog.data.FavouriteOfflineRepository

class FavouriteSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Running offline favourite sync")
        return FavouriteOfflineRepository(applicationContext).flushPending()
            .fold(
                onSuccess = {
                    Log.i(TAG, "Sync complete ($it actions)")
                    Result.success()
                },
                onFailure = {
                    Log.w(TAG, "Sync retry later: ${it.message}")
                    Result.retry()
                }
            )
    }

    companion object {
        const val UNIQUE_NAME = "favourite_sync"
        private const val TAG = "FavouriteSyncWorker"
    }
}
