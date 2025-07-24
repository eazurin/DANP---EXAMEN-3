// In package com.example.examen3.ui.admin

package com.example.examen3.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.examen3.data.PositiveRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostListScreen(
    viewModel: HostListViewModel = viewModel(),
    onHostClick: (String) -> Unit
) {
    val hosts     by viewModel.hosts.collectAsState()
    val positives by viewModel.positives.collectAsState()   // ← NUEVO

    Scaffold(/* ... */) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(hosts) { hostPid ->
                HostItem(
                    pid            = hostPid,
                    isPositive     = hostPid in positives,      // ← NUEVO
                    onClick        = { onHostClick(hostPid) },
                    onMarkPositive = { viewModel.markPositive(hostPid) } // ← NUEVO
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
    onMarkPositive: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive)
                MaterialTheme.colorScheme.error.copy(alpha = .15f)   // rojo tenue
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Host PID: $pid",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            )

            Button(
                onClick = onMarkPositive,
                enabled = !isPositive,                              // ← deshabilita
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (isPositive) "Positivo" else "Marcar COVID‑19")
            }
        }
    }
}