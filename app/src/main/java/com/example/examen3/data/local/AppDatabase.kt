package com.example.examen3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EncounterEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun encounterDao(): EncounterDao
}
