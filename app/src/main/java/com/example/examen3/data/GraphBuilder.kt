package com.example.examen3.data

import com.example.examen3.data.local.EncounterDao
import com.example.examen3.data.model.GraphEdge
import com.example.examen3.data.model.GraphNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class GraphBuilder(private val dao: EncounterDao) {

    /**
     * Devuelve nodos y aristas para el grafo de contagio
     * centrado en [positivePid].
     */
    suspend fun makeGraph(
        positivePid: String
    ): Pair<List<GraphNode>, List<GraphEdge>> = withContext(Dispatchers.IO) {

        val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14)
        val raws  = dao.recentForPid(positivePid, since)

        val grouped = raws.groupBy { it.pidPeer }

        val nodes = mutableListOf(GraphNode(positivePid, "POSITIVE"))
        val edges = mutableListOf<GraphEdge>()

        grouped.forEach { (peer, list) ->
            val totalSec = list.size * 60              // ﻿≈1 min por fila
            val avgRssi  = list.map { it.rssi }.average()
            val distance = 10.0.pow((-59.0 - avgRssi) / 20)   // ﻿≈dist RSSI
            val daysAgo  = (System.currentTimeMillis() - list.maxOf { it.timestamp }) /
                    TimeUnit.DAYS.toMillis(1).toDouble()

            val score = RiskScorer.score(distance, totalSec, daysAgo)
            val level = RiskScorer.riskLevel(score)

            nodes += GraphNode(peer, level)
            edges += GraphEdge(positivePid, peer, level,distance)
        }
        nodes to edges
    }
}
