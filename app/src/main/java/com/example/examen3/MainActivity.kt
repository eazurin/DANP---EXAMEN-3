package com.example.examen3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var proximityService: ProximityService

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startProximityService()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            initializeBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        proximityService = ProximityService()

        setContent {
            MaterialTheme {
                ProximityApp()
            }
        }

        checkPermissions()
    }

    @Composable
    private fun ProximityApp(viewModel: ProximityViewModel = viewModel()) {
        // Arranca en 0 y se actualiza sólo cuando haya un *nuevo* dispositivo
        val uniqueCount by viewModel.uniqueDeviceCount.collectAsState(initial = 0)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "COVID-19 Contact Tracing",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { startProximityService() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Rastreo")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { stopProximityService() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Detener Rastreo")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Aquí mostramos el número de dispositivos *únicos*
                Text(
                    text = "Dispositivos detectados: $uniqueCount",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }


    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ))
        } else {
            permissions.addAll(listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        }

        val needsPermission = permissions.any {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needsPermission) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            initializeBluetooth()
        }
    }

    private fun initializeBluetooth() {
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            startProximityService()
        }
    }

    private fun startProximityService() {
        if (::proximityService.isInitialized) {
            proximityService.startService(this, bluetoothAdapter)
        }
    }

    private fun stopProximityService() {
        if (::proximityService.isInitialized) {
            proximityService.stopService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::proximityService.isInitialized) {
            proximityService.stopService()
        }
    }
}