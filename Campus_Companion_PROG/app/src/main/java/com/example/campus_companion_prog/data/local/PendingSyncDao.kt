package com.example.campus_companion_prog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PendingSyncDao {
    @Insert
    suspend fun insert(action: PendingSyncActionEntity): Long

    @Query("SELECT * FROM pending_sync_actions ORDER BY clientTimestamp ASC")
    suspend fun getAll(): List<PendingSyncActionEntity>

    @Query("DELETE FROM pending_sync_actions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM pending_sync_actions")
    suspend fun count(): Int
}
