package com.example.examen3.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.examen3.data.PositiveRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch            // ← para el ViewModel, ya no en Composable
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EncounterDetailsScreen(
    pidA: String,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToGraph: (String) -> Unit = {}   // opcional
) {
    val viewModel: EncounterDetailsViewModel = viewModel(
        key = pidA,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EncounterDetailsViewModel(pidA) as T
        }
    )

    // 1) Estados observados
    val filteredEncounters by viewModel.state.collectAsState()
    val distanceFilterValue by viewModel.distanceFilter.collectAsState()

    // 2) UI
    Column(Modifier.fillMaxSize()) {

        // -------- FILTRO DE DISTANCIA --------------
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Filtrar hasta:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${String.format("%.1f", distanceFilterValue)} m",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = distanceFilterValue,
                onValueChange = { viewModel.updateDistanceFilter(it) },
                valueRange = 0f..5f,
                steps = 49        // 0.1 m
            )
        }

        Divider()

        // -------- LISTA DE ENCUENTROS --------------
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

/* ---------- ÍTEM (card) ------------- */

@Composable
private fun EncounterItem(
    encounter: Encounter,
    onClick: () -> Unit
) {
    val dateStr = remember(encounter.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(encounter.timestamp))
    }
    val distance = encounter.getDistanceEstimate()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Desde (A): ${encounter.pidA}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text("Con (B): ${encounter.pidB}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "RSSI: ${encounter.rssi} dBm (≈ ${String.format("%.1f", distance)} m)",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text("Fecha: $dateStr", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))


        }
    }
}
