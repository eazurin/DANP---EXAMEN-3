package com.example.examen3.work

import android.content.Context
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.examen3.data.EncounterRepository
import com.example.examen3.data.local.AppDatabase
import com.example.examen3.util.PidStore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db       = Room.databaseBuilder(applicationContext,
            AppDatabase::class.java, "encounters.db").build()
        val dao      = db.encounterDao()
        val repo     = EncounterRepository(
            dao,
            Firebase.firestore,
            PidStore.getMyPid(applicationContext)
        )

        return try {
            repo.syncPending()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val work = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag("syncEncounters")
                .build()

            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
