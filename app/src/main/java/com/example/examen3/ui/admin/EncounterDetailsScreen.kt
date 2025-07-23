// En package com.example.examen3.ui.admin

package com.example.examen3.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EncounterDetailsScreen(
    pidA: String,
    onNavigateToDetails: (String) -> Unit
) {
    val viewModel: EncounterDetailsViewModel = viewModel(
        key = pidA,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EncounterDetailsViewModel(pidA) as T
            }
        }
    )

    val encounters by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(encounters) { encounter ->
            EncounterItem(
                encounter = encounter,
                onClick = { onNavigateToDetails(encounter.pidB) }
            )
        }
    }
}

// EncounterItem actualizado para mostrar PIDs completos y distancia
@Composable
private fun EncounterItem(encounter: Encounter, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(encounter.timestamp))

    // 1. Calculamos la distancia usando la función del modelo
    val distance = encounter.getDistanceEstimate()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // 2. Mostramos los PIDs completos, sin el .take(8)
            Text(text = "Desde (A): ${encounter.pidA}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Con (B): ${encounter.pidB}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            // 3. Mostramos el RSSI y la distancia formateada a un decimal
            Text(
                text = "RSSI: ${encounter.rssi} dBm (≈ ${String.format("%.1f", distance)}m)",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fecha: $dateStr", style = MaterialTheme.typography.bodySmall)
        }
    }
}