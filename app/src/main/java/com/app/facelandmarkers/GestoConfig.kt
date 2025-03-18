package com.app.facelandmarkers

data class GestoConfig (
    val textoSalida: String = "",
    val threshold: Float = 0.0f
)

data class GestoSnapshot(
    val nombre: String,
    val landmarks: Map<String, Float>
)