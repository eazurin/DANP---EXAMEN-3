package com.example.examen3.data.model

import com.google.firebase.Timestamp

/** Documento que se guarda en la colección “positives”. */
data class Positive(
    val pid: String = "",
    val diagnosisDate: Timestamp = Timestamp.now()
)
