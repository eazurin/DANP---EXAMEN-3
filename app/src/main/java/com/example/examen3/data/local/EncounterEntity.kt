package com.example.examen3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encounters_local")
data class EncounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pidPeer: String,
    val rssi: Int,
    val timestamp: Long,
    val synced: Boolean = false
)
