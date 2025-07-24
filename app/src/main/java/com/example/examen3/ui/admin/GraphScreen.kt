package com.example.examen3.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.*

@Composable
fun GraphScreen(positivePid: String) {

    val vm: GraphViewModel = viewModel(
        key = positivePid,
        initializer = { GraphViewModel(positivePid) }   // sin Factory manual
    )
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
        if (state.nodes.isEmpty())
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        else
            ContagionGraph(state)
    }
}

@Composable
private fun ContagionGraph(state: GraphState) {
    val radius     = 140.dp
    val nodeRadius = 18f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val n  = state.nodes.size - 1
        val cx = size.width / 2
        val cy = size.height / 2

        fun pos(i: Int): Offset =
            if (i == 0) Offset(cx, cy)
            else {
                val ang = 2 * PI * (i - 1) / n - PI / 2
                Offset(
                    cx + radius.toPx() * cos(ang).toFloat(),
                    cy + radius.toPx() * sin(ang).toFloat()
                )
            }

        /* Aristas */
        state.edges.forEach { e ->
            val from = state.nodes.indexOfFirst { it.pid == e.from }
            val to   = state.nodes.indexOfFirst { it.pid == e.to }
            drawLine(
                color = when (e.level) {
                    "HIGH"   -> Color.Red
                    "MEDIUM" -> Color(0xFFFFA000)
                    else     -> Color.Gray
                },
                start = pos(from),
                end   = pos(to),
                strokeWidth = 4f
            )
        }

        /* Nodos */
        state.nodes.forEachIndexed { idx, node ->
            drawCircle(
                color = when (node.type) {
                    "POSITIVE" -> Color.Red
                    "HIGH"     -> Color.Red.copy(alpha = .6f)
                    "MEDIUM"   -> Color(0xFFFFA000)
                    else       -> Color.Gray
                },
                radius = if (idx == 0) nodeRadius + 7 else nodeRadius,
                center = pos(idx)
            )
        }
    }
}
