package com.example.examen3.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostListScreen(
    viewModel: HostListViewModel = viewModel(),
    onHostClick:  (String) -> Unit,
    onGraphClick: (String) -> Unit
) {
    val hosts     by viewModel.hosts.collectAsState()
    val positives by viewModel.positives.collectAsState()   // 🔴 PIDs marcados

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dispositivos Rastreados (Hosts)") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(hosts) { pid ->
                HostItem(
                    pid          = pid,
                    isPositive   = pid in positives,
                    onClick      = { onHostClick(pid) },
                    onMark       = {
                        viewModel.markPositive(pid)   // ① graba en Firestore
                        onGraphClick(pid)             // ② navega al grafo
                    }
                )
            }
        }
    }
}

@Composable
private fun HostItem(
    pid: String,
    isPositive: Boolean,
    onClick: () -> Unit,
    onMark: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive)
                MaterialTheme.colorScheme.error.copy(alpha = .15f)   // fondo rojo tenue
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            /* PID clicable para ver detalles */
            Text(
                text = "Host PID: $pid",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            )

            /* Botón Marcar COVID‑19 */
            Button(
                onClick  = onMark,
                enabled  = !isPositive,              // ← DESHABILITA tras marcar
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Marcar COVID‑19") }
        }
    }
}
