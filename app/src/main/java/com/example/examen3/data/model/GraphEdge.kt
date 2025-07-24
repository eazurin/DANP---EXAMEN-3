package com.example.examen3.data.model

data class GraphEdge(
    val from: String,
    val to:   String,
    val level: String,
    val distance: Double          // ← NUEVO
)

