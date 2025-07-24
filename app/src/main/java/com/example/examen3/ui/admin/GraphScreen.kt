package com.example.examen3.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.*

@Composable
fun GraphScreen(positivePid: String) {
    val vm: GraphViewModel = viewModel(
        key = positivePid,
        initializer = { GraphViewModel(positivePid) }
    )
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
        if (state.nodes.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            ContagionGraph(state)
        }
    }
}

@Composable
private fun ContagionGraph(state: GraphState) {
    val radiusDp    = 140.dp
    val nodeRadius  = 18f
    val density     = LocalDensity.current

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val radiusPx = with(density) { radiusDp.toPx() }
        val n        = state.nodes.size - 1
        val cx       = size.width  / 2
        val cy       = size.height / 2

        fun pos(i: Int): Offset =
            if (i == 0) Offset(cx, cy)
            else {
                val ang = 2 * PI * (i - 1) / n - PI / 2
                Offset(
                    cx + radiusPx * cos(ang).toFloat(),
                    cy + radiusPx * sin(ang).toFloat()
                )
            }

        // 1) Aristas + distancia
        state.edges.forEach { e ->
            val fromIdx = state.nodes.indexOfFirst { it.pid == e.from }
            val toIdx   = state.nodes.indexOfFirst { it.pid == e.to }
            val start   = pos(fromIdx)
            val end     = pos(toIdx)

            // línea
            drawLine(
                color = when (e.level) {
                    "HIGH"   -> Color.Red
                    "MEDIUM" -> Color(0xFFFFA000)
                    else     -> Color.Gray
                },
                start = start,
                end   = end,
                strokeWidth = 4f
            )

            // etiqueta distancia en el punto medio
            val mid = Offset((start.x + end.x) / 2, (start.y + end.y) / 2)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "${"%.1f".format(e.distance)} m",
                    mid.x,
                    mid.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // 2) Nodos + PID
        state.nodes.forEachIndexed { idx, node ->
            val center = pos(idx)
            drawCircle(
                color = when (node.type) {
                    "POSITIVE" -> Color.Red
                    "HIGH"     -> Color.Red.copy(alpha = .6f)
                    "MEDIUM"   -> Color(0xFFFFA000)
                    else       -> Color.Gray
                },
                radius = if (idx == 0) nodeRadius + 7 else nodeRadius,
                center = center
            )

            // etiqueta PID (primeros 6 caracteres)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    node.pid.take(6),
                    center.x,
                    center.y + nodeRadius + 24,    // ajusta verticalmente
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
