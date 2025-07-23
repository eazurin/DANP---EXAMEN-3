// En package com.example.examen3.ui.admin

package com.example.examen3.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data class actualizada para incluir el cálculo de distancia
data class Encounter(
    val id: String,
    val pidA: String,
    val pidB: String,
    val rssi: Int,
    val timestamp: Long
) {
    /**
     * Calcula la distancia estimada en metros a partir del RSSI.
     * Esta es la misma fórmula de tu clase ProximityEvent.
     */
    fun getDistanceEstimate(): Double {
        val txPower = -59.0 // Potencia de transmisión de referencia a 1 metro
        val pathLossExponent = 2.0 // Factor de pérdida de señal en espacio abierto
        // Fórmula: Distancia = 10^((TX_Power - RSSI) / (10 * N))
        return Math.pow(10.0, (txPower - rssi) / (10 * pathLossExponent))
    }
}

// El resto de la clase EncounterDetailsViewModel no cambia
class EncounterDetailsViewModel(private val pidA: String) : ViewModel() {

    private val _state = MutableStateFlow<List<Encounter>>(emptyList())
    val state: StateFlow<List<Encounter>> = _state.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        if (pidA.isNotBlank()) {
            viewModelScope.launch {
                encountersFlow()
                    .collect { _state.value = it }
            }
        }
    }

    private fun encountersFlow(): Flow<List<Encounter>> = callbackFlow {
        val listener = firestore.collection("encounters")
            .whereEqualTo("pid_a", pidA)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("EncounterDetailsVM", "Error listening for encounters for $pidA", error)
                    return@addSnapshotListener
                }

                val docs = snap?.documents ?: emptyList()
                val list = docs.map { d ->
                    val pidA = d.getString("pid_a") ?: "<sin pid_a>"
                    val pidB = d.getString("pid_b") ?: "<sin pid_b>"
                    val rssi = d.getLong("rssi")?.toInt() ?: 0
                    val ts: Long = d.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                    Encounter(
                        id = d.id,
                        pidA = pidA,
                        pidB = pidB,
                        rssi = rssi,
                        timestamp = ts
                    )
                }
                trySend(list).isSuccess
            }
        awaitClose { listener.remove() }
    }
}