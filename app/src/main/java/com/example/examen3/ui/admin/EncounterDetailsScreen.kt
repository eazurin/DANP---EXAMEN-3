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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    // Recolectamos ambos estados desde el ViewModel
    val filteredEncounters by viewModel.state.collectAsState()
    val distanceFilterValue by viewModel.distanceFilter.collectAsState()

    // Usamos un Column para poner el Slider encima de la lista
    Column(modifier = Modifier.fillMaxSize()) {
        //  UI DEL FILTRO
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Filtrar hasta:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${String.format("%.1f", distanceFilterValue)} metros",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = distanceFilterValue,
                onValueChange = { viewModel.updateDistanceFilter(it) },
                valueRange = 0f..5f,
                // Pasos para permitir decimales (50 pasos en un rango de 5 = pasos de 0.1)
                steps = 49
            )
        }


        Divider() // Un separador visual

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredEncounters) { encounter ->
                EncounterItem(
                    encounter = encounter,
                    onClick = { onNavigateToDetails(encounter.pidB) }
                )
            }
        }
    }
}

@Composable
private fun EncounterItem(encounter: Encounter, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(encounter.timestamp))

    val distance = encounter.getDistanceEstimate()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = "Desde (A): ${encounter.pidA}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Con (B): ${encounter.pidB}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RSSI: ${encounter.rssi} dBm (≈ ${String.format("%.1f", distance)}m)",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fecha: $dateStr", style = MaterialTheme.typography.bodySmall)
        }
    }
}