package com.example.examen3.data

import android.content.Context
import android.content.SharedPreferences
import com.example.examen3.data.local.EncounterDao
import com.example.examen3.data.model.ExposureFlag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cruza los encuentros locales con la lista de positivos
 * y genera flags de exposición si hay coincidencias nuevas.
 */
class ExposureDetector(
    private val ctx: Context,
    private val dao: EncounterDao,
    private val positives: List<String>
) {
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("exposure_prefs", Context.MODE_PRIVATE)

    suspend fun detectAndSave(): List<ExposureFlag> = withContext(Dispatchers.IO) {
        if (positives.isEmpty()) return@withContext emptyList<ExposureFlag>()

        val recent = dao.pending(Int.MAX_VALUE)              // o filtra por fecha si quieres
        val hits   = recent.filter { it.pidPeer in positives }

        val flags = hits.map { e ->
            val risk = when {
                e.rssi >= -65 -> "HIGH"
                e.rssi >= -70 -> "MEDIUM"
                else          -> "LOW"
            }
            ExposureFlag(e.pidPeer, e.timestamp, risk)
        }

        // guarda solo los que nunca vimos
        val newFlags = flags.filter { !prefs.contains(it.positivePid) }
        newFlags.forEach { f ->
            prefs.edit().putLong(f.positivePid, f.timestamp).apply()
        }
        newFlags
    }
}
