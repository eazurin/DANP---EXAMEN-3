package com.example.examen3.work

import android.content.Context
import androidx.work.*
import com.example.examen3.data.ExposureDetector
import com.example.examen3.data.PositiveRepository
import com.example.examen3.data.local.AppDatabase
import com.example.examen3.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Descarga la lista de positivos, cruza con encuentros locales
 * y genera notificación (o sólo flag) si hay coincidencias nuevas.
 */
class ExposureWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val fs        = FirebaseFirestore.getInstance()
        val positives = PositiveRepository(fs).fetchAllPositives()

        val dao   = AppDatabase.getInstance(applicationContext).encounterDao()
        val flags = ExposureDetector(applicationContext, dao, positives).detectAndSave()

        if (flags.isNotEmpty()) {
            NotificationHelper.showExposureAlert(applicationContext, flags.size)
        }
        Result.success()
    }

    companion object {
        fun enqueue(ctx: Context) {
            val req = OneTimeWorkRequestBuilder<ExposureWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("exposureSync")
                .build()
            WorkManager.getInstance(ctx).enqueue(req)
        }
    }
}
