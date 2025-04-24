package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Tile(
    val id: Int,
    val type: TileType,
    val value: Int,
    val coordinates: List<Double> // [x, y]
)