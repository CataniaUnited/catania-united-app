package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PortTransform(
    val x: Double,
    val y: Double,
    val rotation: Double
)