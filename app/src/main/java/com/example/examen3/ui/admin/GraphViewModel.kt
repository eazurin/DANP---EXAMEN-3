package com.example.examen3.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen3.data.RiskScorer
import com.example.examen3.data.model.GraphEdge
import com.example.examen3.data.model.GraphNode
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class GraphViewModel(private val positivePid: String) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(GraphState())
    val state: StateFlow<GraphState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            encountersFlow()
                .map { buildGraph(it) }
                .collect { _state.value = it }
        }
    }
    private fun encountersFlow(): Flow<List<EncounterDoc>> = flowOf(emptyList())

    /*
    /**  */
    private fun encountersFlow(): Flow<List<EncounterDoc>> = callbackFlow {
        val since = Timestamp((System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(14)) / 1000, 0)

        val a = firestore.collection("encounters")
            .whereEqualTo("pid_a", positivePid)
            .whereGreaterThan("timestamp", since)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull(::mapDoc) ?: emptyList()).isSuccess
            }

        val b = firestore.collection("encounters")
            .whereEqualTo("pid_b", positivePid)
            .whereGreaterThan("timestamp", since)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull(::mapDoc) ?: emptyList()).isSuccess
            }

        awaitClose { a.remove(); b.remove() }
    }.conflate()
*/
    private fun mapDoc(d: com.google.firebase.firestore.DocumentSnapshot): EncounterDoc? =
        d.getLong("rssi")?.toInt()?.let { rssi ->
            EncounterDoc(
                pidA = d.getString("pid_a") ?: return null,
                pidB = d.getString("pid_b") ?: return null,
                rssi = rssi,
                timestamp = d.getTimestamp("timestamp")?.toDate()?.time ?: return null
            )
        }

    private fun buildGraph(list: List<EncounterDoc>): GraphState {
        if (list.isEmpty())
            return GraphState(listOf(GraphNode(positivePid, "POSITIVE")), emptyList())

        val grouped = list.groupBy { if (it.pidA == positivePid) it.pidB else it.pidA }

        val nodes = mutableListOf(GraphNode(positivePid, "POSITIVE"))
        val edges = mutableListOf<GraphEdge>()

        grouped.forEach { (peer, encs) ->
            val totalSec = encs.size * 60             // ≈1 min/doc
            val avgRssi  = encs.map { it.rssi }.average()
            val distance = 10.0.pow((-59 - avgRssi) / 20)
            val daysAgo  = (System.currentTimeMillis() -
                    encs.maxOf { it.timestamp }) /
                    TimeUnit.DAYS.toMillis(1).toDouble()

            val score = RiskScorer.score(distance, totalSec, daysAgo)
            val level = RiskScorer.riskLevel(score)

            nodes += GraphNode(peer, level)
            edges += GraphEdge(positivePid, peer, level)
        }
        return GraphState(nodes, edges)
    }

    private data class EncounterDoc(
        val pidA: String,
        val pidB: String,
        val rssi: Int,
        val timestamp: Long
    )
}
