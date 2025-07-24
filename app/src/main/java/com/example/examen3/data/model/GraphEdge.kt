package com.example.examen3.data.model

/** Arista del grafo mostrado en la UI admin. */
data class GraphEdge(
    val from: String,
    val to:   String,
    val level: String          // HIGH / MEDIUM / LOW
)
