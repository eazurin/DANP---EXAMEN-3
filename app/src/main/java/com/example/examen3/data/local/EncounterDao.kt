package com.example.examen3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EncounterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EncounterEntity)

    @Query("SELECT * FROM encounters_local WHERE synced = 0 LIMIT :limit")
    suspend fun pending(limit: Int): List<EncounterEntity>

    @Query("UPDATE encounters_local SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM encounters_local WHERE synced = 1 AND timestamp < :limit")
    suspend fun purgeOld(limit: Long)
    /** Encuentros de un PID en los últimos días (para el grafo). */
    @Query("SELECT * FROM encounters_local WHERE pidPeer = :pidPeer AND timestamp >= :since")
    suspend fun recentForPid(pidPeer: String, since: Long): List<EncounterEntity>
}
