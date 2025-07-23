// En package com.example.examen3.ui.admin

package com.example.examen3.ui.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.examen3.ui.theme.Examen3Theme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Examen3Theme {
                AdminNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavigation() {
    val navController = rememberNavController()
    // Observamos la ruta actual para actualizar la UI (título y botones)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // El PID actual, extraído de la ruta para mostrarlo en el título
    val currentPid = backStackEntry?.arguments?.getString("pidA") ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Título dinámico según la pantalla
                    val title = if (currentRoute == "host_list") {
                        "Dispositivos Rastreados"
                    } else {
                        "Encuentros de: ${currentPid.take(8)}..." // Acortamos el PID
                    }
                    Text(title)
                },
                navigationIcon = {
                    // Mostramos el botón de retroceder solo en la pantalla de detalles
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retroceder"
                            )
                        }
                    }
                },
                actions = {
                    // Mostramos el botón de Home solo en la pantalla de detalles
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = {
                            // Navega al inicio y limpia la pila de navegación
                            navController.popBackStack("host_list", inclusive = false)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Ir al Inicio"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "host_list",
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable("host_list") {
                HostListScreen(
                    onHostClick = { pidA ->
                        navController.navigate("encounter_details/$pidA")
                    }
                )
            }
            composable(
                route = "encounter_details/{pidA}",
                arguments = listOf(navArgument("pidA") { type = NavType.StringType })
            ) {
                val pidA = it.arguments?.getString("pidA") ?: ""
                EncounterDetailsScreen(
                    pidA = pidA,
                    // Pasamos la acción de navegación para la navegación iterativa
                    onNavigateToDetails = { nextPid ->
                        navController.navigate("encounter_details/$nextPid")
                    }
                )
            }
        }
    }
}