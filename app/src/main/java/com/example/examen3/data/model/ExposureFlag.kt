package com.example.examen3.data.model

/** Marca local que indica que este dispositivo estuvo expuesto a un caso positivo. */
data class ExposureFlag(
    val positivePid: String,
    val timestamp: Long,
    val riskLevel: String           // HIGH / MEDIUM / LOW
)
