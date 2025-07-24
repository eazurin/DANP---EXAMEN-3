package com.example.examen3.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen3.data.RiskScorer
import com.example.examen3.data.model.GraphEdge
import com.example.examen3.data.model.GraphNode
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import com.google.firebase.firestore.ktx.snapshots   // 🔸 añádelo



class GraphViewModel(private val positivePid: String) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(GraphState())
    val state: StateFlow<GraphState> = _state

    init {
        viewModelScope.launch {
            encountersFlow()
                .map(::buildGraph)
                .collect { _state.value = it }
        }
    }

    /** Usa snapshots() → Flow<QuerySnapshot> (sin callbackFlow) */
    private fun encountersFlow(): Flow<List<EncounterDoc>> {
        val since = Timestamp(
            (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14)) / 1000,
            0
        )

        val flowA = firestore.collection("encounters")
            .whereEqualTo("pid_a", positivePid)
            .whereGreaterThan("timestamp", since)
            .snapshots()                                // ← requiere coroutines‑play‑services
            .map { qs -> qs.documents.mapNotNull(::mapDoc) }

        val flowB = firestore.collection("encounters")
            .whereEqualTo("pid_b", positivePid)
            .whereGreaterThan("timestamp", since)
            .snapshots()
            .map { qs -> qs.documents.mapNotNull(::mapDoc) }

        return merge(flowA, flowB).conflate()
    }

    private fun mapDoc(d: com.google.firebase.firestore.DocumentSnapshot) =
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
            val totalSec = encs.size * 60
            val avgRssi  = encs.map { it.rssi }.average()
            val distance = 10.0.pow((-59 - avgRssi) / 20)
            val daysAgo  = (System.currentTimeMillis() -
                    encs.maxOf { it.timestamp }) /
                    TimeUnit.DAYS.toMillis(1).toDouble()

            val score = RiskScorer.score(distance, totalSec, daysAgo)
            val level = RiskScorer.riskLevel(score)

            nodes += GraphNode(peer, level)
            edges += GraphEdge(positivePid, peer, level,
                distance  = distance)
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
