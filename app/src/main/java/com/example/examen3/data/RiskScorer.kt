package com.example.examen3.data

import kotlin.math.exp

/**
 * Combina distancia, duración y recencia para producir
 * un puntaje 0-100 y un nivel de riesgo.
 */
object RiskScorer {

    /** Distancia → 0-1 */
    private fun proximityScore(d: Double): Double =
        when {
            d <= 2   -> 1.0
            d <= 5   -> 0.7
            d <= 10  -> 0.4
            else     -> 0.1
        }

    /** Duración (seg) → 0-1 */
    private fun durationScore(seconds: Int): Double =
        1.0 - exp(-seconds / 900.0)          // ﻿≈0.8 a 15 min

    /** Días desde el último encuentro → 0-1 */
    private fun recencyScore(daysAgo: Double): Double =
        exp(-daysAgo / 3.0)                  // ﻿mitad cada ~3 días

    fun score(distanceM: Double, totalSeconds: Int, daysAgo: Double): Int {
        val w1 = 0.45  // distancia
        val w2 = 0.35  // duración
        val w3 = 0.20  // recencia

        val s = w1 * proximityScore(distanceM) +
                w2 * durationScore(totalSeconds) +
                w3 * recencyScore(daysAgo)
        return (s * 100).toInt()
    }

    fun riskLevel(score: Int): String =
        when {
            score >= 70 -> "HIGH"
            score >= 40 -> "MEDIUM"
            else        -> "LOW"
        }
}
