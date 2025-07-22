package com.example.examen3

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class ProximityService {

    companion object {
        private const val TAG = "ProximityService"
        private const val SERVICE_UUID = "0000180F-0000-1000-8000-00805F9B34FB" // Battery Service UUID
        private const val PROXIMITY_THRESHOLD_RSSI = -70 // dBm para ~30 metros
        private const val SCAN_PERIOD = 10000L // 10 segundos
        private const val SCAN_INTERVAL_MS = 30000L // Escanear cada 30 segundos
        private const val ADVERTISE_INTERVAL_MS = 100 // 100ms para advertising
        private const val TX_POWER_LEVEL = AdvertiseSettings.ADVERTISE_TX_POWER_LOW
    }

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false
    private var isAdvertising = false

    // Cola de eventos de proximidad
    private val proximityEventsQueue = ConcurrentLinkedQueue<ProximityEvent>()

    // Handler para optimizar el consumo de batería
    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null

    // Callback para advertising
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising iniciado exitosamente")
            isAdvertising = true
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "Error al iniciar advertising: $errorCode")
            isAdvertising = false
        }
    }

    // Callback para scanning
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                handleScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "Error en scanning: $errorCode")
            isScanning = false
        }
    }

    fun startService(context: Context, bluetoothAdapter: BluetoothAdapter) {
        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            Log.e(TAG, "Advertising no soportado en este dispositivo")
            return
        }

        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (bluetoothLeAdvertiser == null || bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLE advertiser o scanner no disponible")
            return
        }

        startAdvertising()
        startPeriodicScanning()
    }

    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(TX_POWER_LEVEL)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
            .addServiceData(ParcelUuid.fromString(SERVICE_UUID), generateDeviceId())
            .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private fun startPeriodicScanning() {
        scanRunnable = object : Runnable {
            override fun run() {
                if (isScanning) {
                    stopScanning()
                }
                startScanning()

                // Detener el escaneo después de SCAN_PERIOD
                handler.postDelayed({
                    stopScanning()
                }, SCAN_PERIOD)

                // Programar el siguiente ciclo de escaneo
                handler.postDelayed(this, SCAN_INTERVAL_MS)
            }
        }

        handler.post(scanRunnable!!)
    }

    private fun startScanning() {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setReportDelay(0)
            .build()

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
                .build()
        )

        bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        isScanning = true
        Log.d(TAG, "Scanning iniciado")
    }

    private fun stopScanning() {
        if (isScanning) {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
            Log.d(TAG, "Scanning detenido")
        }
    }

    // Reemplaza TODO el método por esto:
    private fun handleScanResult(result: ScanResult) {
        val address = result.device.address
        val rssi = result.rssi

        Log.d(TAG, "Dispositivo detectado: $address, RSSI: $rssi dBm")

        // 1️⃣ Filtrar por proximidad
        if (rssi < PROXIMITY_THRESHOLD_RSSI) {
            Log.d(TAG, "⛔ Descarta: RSSI ($rssi) < umbral ($PROXIMITY_THRESHOLD_RSSI)")
            return
        }

        // 2️⃣ Usar la MAC como ID (sin los ':')
        val deviceId = address.replace(":", "")
        Log.d(TAG, "✔ Registrando evento con ID de MAC: $deviceId")

        // 3️⃣ Construir y postear el evento
        val proximityEvent = ProximityEvent(
            deviceId   = deviceId,
            rssi       = rssi,
            timestamp  = System.currentTimeMillis(),
            duration   = calculateEncounterDuration(deviceId)
        )
        Log.d(TAG, "Evento de proximidad agregado: $proximityEvent")
        ProximityEventBus.postEvent(proximityEvent)
    }


    private fun generateDeviceId(): ByteArray {
        // Generar un ID único anónimo para el dispositivo
        val uuid = UUID.randomUUID()
        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
    }

    private fun extractDeviceIdFromScanRecord(scanRecord: ScanRecord?): String? {
        scanRecord?.let { record ->
            val serviceData = record.getServiceData(ParcelUuid.fromString(SERVICE_UUID))
            serviceData?.let { data ->
                return UUID.nameUUIDFromBytes(data).toString()
            }
        }
        return null
    }

    private fun calculateEncounterDuration(deviceId: String): Long {
        // Implementación simplificada - en una versión real se mantendría
        // un registro de cuándo se detectó por primera vez cada dispositivo
        return 60000L // 1 minuto por defecto
    }

    fun stopService() {
        // Detener advertising
        if (isAdvertising) {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            isAdvertising = false
        }

        // Detener scanning
        stopScanning()

        // Cancelar el runnable programado
        scanRunnable?.let { handler.removeCallbacks(it) }

        Log.d(TAG, "ProximityService detenido")
    }

    fun getProximityEvents(): List<ProximityEvent> {
        return proximityEventsQueue.toList()
    }

    fun clearProximityEvents() {
        proximityEventsQueue.clear()
    }
}