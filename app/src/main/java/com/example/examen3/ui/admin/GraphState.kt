package com.example.examen3.ui.admin

import com.example.examen3.data.model.GraphEdge
import com.example.examen3.data.model.GraphNode

data class GraphState(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList()
)
