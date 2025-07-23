package com.example.examen3.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Encounter(
    val id: String,
    val pidA: String,
    val pidB: String,
    val rssi: Int,
    val timestamp: Long
)

class AdminViewModel : ViewModel() {

    private val _state = MutableStateFlow<List<Encounter>>(emptyList())
    val state: StateFlow<List<Encounter>> = _state.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        viewModelScope.launch {
            encountersFlow()
                .collect { _state.value = it }
        }
    }

    private fun encountersFlow(): Flow<List<Encounter>> = callbackFlow {
        val listener = firestore.collection("encounters")
            // Ordenamos por timestamp para ver primero los más recientes
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("AdminViewModel", "Error al escuchar encounters", error)
                    return@addSnapshotListener
                }

                val docs = snap?.documents ?: emptyList()
                Log.d("AdminViewModel", "Snapshot de encounters recibido: ${docs.size} documentos")

                val list = docs.map { d ->
                    // Leer campos pid_a y pid_b
                    val pidA = d.getString("pid_a") ?: "<sin pid_a>"
                    val pidB = d.getString("pid_b") ?: "<sin pid_b>"

                    // Leer RSSI como entero
                    val rssi = d.getLong("rssi")?.toInt() ?: 0

                    // Leer timestamp:
                    // – Si usas Firestore Timestamp:
                    val ts: Long = d.getTimestamp("timestamp")
                        ?.toDate()
                        ?.time
                    // – Si lo guardas como String ISO 8601:
                        ?: d.getString("timestamp")?.let { str ->
                            try {
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                                    .parse(str)
                                    ?.time
                            } catch (e: Exception) {
                                null
                            }
                        }
                        ?: 0L

                    // Loguear cada registro para debugging
                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(ts))
                    Log.d("AdminViewModel", "  • $pidA ↔ $pidB  RSSI=$rssi  @ $dateStr")

                    Encounter(
                        id        = d.id,
                        pidA      = pidA,
                        pidB      = pidB,
                        rssi      = rssi,
                        timestamp = ts
                    )
                }

                trySend(list).isSuccess
            }

        awaitClose { listener.remove() }
    }
}
