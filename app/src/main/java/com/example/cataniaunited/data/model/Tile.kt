package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Tile(
    val id: Int,
    val type: TileType,
    val value: Int,
    val coordinates: List<Double>,  // [x, y]
    val isRobbed : Boolean = false
)