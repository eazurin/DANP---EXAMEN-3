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

data class Encounter(
    val id: String,
    val pidA: String,
    val pidB: String,
    val rssi: Int,
    val timestamp: Long
) {
    fun getDistanceEstimate(): Double {
        val txPower = -59.0
        val pathLossExponent = 2.0
        return Math.pow(10.0, (txPower - rssi) / (10 * pathLossExponent))
    }
}

class EncounterDetailsViewModel(private val pidA: String) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _distanceFilter = MutableStateFlow(5.0f)
    val distanceFilter: StateFlow<Float> = _distanceFilter.asStateFlow()

    private val rawEncountersFlow: Flow<List<Encounter>> = callbackFlow {
        if (pidA.isBlank()) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("encounters")
            .whereEqualTo("pid_a", pidA)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("EncounterDetailsVM", "Error listening for encounters for $pidA", error)
                    close(error)
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { d ->
                    Encounter(
                        id = d.id,
                        pidA = d.getString("pid_a") ?: "<sin pid_a>",
                        pidB = d.getString("pid_b") ?: "<sin pid_b>",
                        rssi = d.getLong("rssi")?.toInt() ?: 0,
                        timestamp = d.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    )
                } ?: emptyList()

                trySend(list).isSuccess
            }
        awaitClose { listener.remove() }
    }

    val state: StateFlow<List<Encounter>> = combine(rawEncountersFlow, distanceFilter) { encounters, distance ->
        encounters.filter { it.getDistanceEstimate() <= distance }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateDistanceFilter(newDistance: Float) {
        _distanceFilter.value = newDistance
    }
}