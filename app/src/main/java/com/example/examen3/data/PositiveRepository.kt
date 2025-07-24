package com.example.examen3.data

import com.example.examen3.data.model.Positive
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PositiveRepository(private val fs: FirebaseFirestore) {

    /** Crea/actualiza el documento “positives/{pid}”. */
    suspend fun markPositive(pid: String) {
        fs.collection("positives").document(pid)
            .set(Positive(pid = pid)).await()
    }

    /** Devuelve TODOS los PIDs positivos. */
    suspend fun fetchAllPositives(): List<String> =
        fs.collection("positives").get().await()
            .documents.mapNotNull { it.getString("pid") }
}
