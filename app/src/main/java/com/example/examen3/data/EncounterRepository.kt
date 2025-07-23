package com.example.examen3.data

import com.example.examen3.data.local.EncounterDao
import com.example.examen3.data.local.EncounterEntity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class EncounterRepository(
    private val dao: EncounterDao,
    private val firestore: FirebaseFirestore,
    private val myPid: String
) {
    suspend fun saveLocal(pidPeer: String, rssi: Int) {
        dao.insert(
            EncounterEntity(pidPeer = pidPeer, rssi = rssi, timestamp = System.currentTimeMillis())
        )
    }

    suspend fun syncPending() {
        val batchLocal = dao.pending(500)
        if (batchLocal.isEmpty()) return

        firestore.runBatch { wb ->
            batchLocal.forEach { e ->
                wb.set(
                    firestore.collection("encounters").document(),
                    mapOf(
                        "timestamp" to Timestamp(e.timestamp / 1000, ((e.timestamp % 1000) * 1_000_000).toInt()),
                        "rssi"      to e.rssi,
                        "pid_a"     to myPid,
                        "pid_b"     to e.pidPeer
                    )
                )
            }
        }.await()

        dao.markSynced(batchLocal.map { it.id })
        dao.purgeOld(System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(2))
    }
}
