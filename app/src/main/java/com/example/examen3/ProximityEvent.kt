package com.example.examen3

data class ProximityEvent(
    val deviceId: String,
    val rssi: Int,
    val timestamp: Long,
    val duration: Long
) {
    fun getDistanceEstimate(): Double {
        // Fórmula aproximada para estimar distancia basada en RSSI
        // Distancia = 10^((TX_Power - RSSI) / (10 * N))
        // donde TX_Power ≈ -59 dBm a 1 metro, N ≈ 2 (factor de pérdida de trayectoria)
        val txPower = -59.0 // dBm a 1 metro
        val pathLossExponent = 2.0
        return Math.pow(10.0, (txPower - rssi) / (10 * pathLossExponent))
    }

    fun isWithinProximityRange(): Boolean {
        return rssi >= -70 // Aproximadamente 30 metros
    }

    override fun toString(): String {
        return "ProximityEvent(deviceId='$deviceId', rssi=$rssi dBm, distance≈${String.format("%.1f", getDistanceEstimate())}m, timestamp=$timestamp, duration=${duration}ms)"
    }
}