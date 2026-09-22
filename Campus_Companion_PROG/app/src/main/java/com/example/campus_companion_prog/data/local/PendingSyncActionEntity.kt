package com.example.campus_companion_prog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_actions")
data class PendingSyncActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String, // SAVE | REMOVE
    val itemType: String, // LOCATION | EVENT
    val itemId: String,
    val clientTimestamp: Long
)
